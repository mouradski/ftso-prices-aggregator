package dev.mouradski.ftso.trades.client.lbank;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;

import javax.websocket.ClientEndpoint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
@ClientEndpoint

public class LbankClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://www.lbkex.net/ws/V2/";
    }

    @Override
    protected void subscribe() {
        getAssets(true).forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> this.sendMessage("{\"action\":\"subscribe\", \"subscribe\":\"trade\", \"pair\":\"SYMBOL_QUOTE\"}".replaceAll("SYMBOL", base).replace("QUOTE", quote))));
    }

    @Override
    protected String getExchange() {
        return "lbank";
    }

    @Override
    protected boolean pong(String message) {
        if (message.contains("ping")) {
            this.sendMessage(message.replaceAll("ping", "pong"));
            return true;
        }

        return false;
    }

    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {

        if (!message.contains("\"trade\"")) {
            return new ArrayList<>();
        }

        var tradeWrapper = objectMapper.readValue(message, TradeWrapper.class);

        var pair = SymbolHelper.getPair(tradeWrapper.getPair());

        return Arrays.asList(Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).price(tradeWrapper.getTrade().getPrice()).amount(tradeWrapper.getTrade().getAmount()).timestamp(currentTimestamp()).build());
    }

    @Scheduled(every="30s")
    public void ping() {
        this.sendMessage("{\"ping\":\"ID\",\"action\":\"ping\"}".replace("ID", incAndGetIdAsString()));
    }

}
