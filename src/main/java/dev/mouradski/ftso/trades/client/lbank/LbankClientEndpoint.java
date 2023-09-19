package dev.mouradski.ftso.trades.client.lbank;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Ticker;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@ClientEndpoint
@Startup
public class LbankClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://www.lbkex.net/ws/V2/";
    }

    @Override
    protected void subscribeTrade() {
        getAssets(true).forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> this.sendMessage("{\"action\":\"subscribe\", \"subscribe\":\"trade\", \"pair\":\"SYMBOL_QUOTE\"}".replaceAll("SYMBOL", base).replace("QUOTE", quote))));
    }

    @Override
    protected void subscribeTicker() {
        getAssets(true).forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> this.sendMessage("{\"action\":\"subscribe\", \"subscribe\":\"tick\", \"pair\":\"SYMBOL_QUOTE\"}".replaceAll("SYMBOL", base).replace("QUOTE", quote))));
    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("tick")) {
            return Optional.empty();
        }


        var tickerData = objectMapper.readValue(message, TickerData.class);

        var pair = SymbolHelper.getPair(tickerData.getPair());

        return Optional.of(Collections.singletonList(Ticker.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(tickerData.getTick().getLatest()).timestamp(currentTimestamp()).build()));
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
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {

        if (!message.contains("\"trade\"")) {
            return Optional.empty();
        }

        var tradeWrapper = objectMapper.readValue(message, TradeWrapper.class);

        var pair = SymbolHelper.getPair(tradeWrapper.getPair());

        return Optional.of(Collections.singletonList(Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).price(tradeWrapper.getTrade().getPrice()).amount(tradeWrapper.getTrade().getVolume()).timestamp(currentTimestamp()).build()));
    }

    @Scheduled(every="30s")
    public void ping() {
        this.sendMessage("{\"ping\":\"ID\",\"action\":\"ping\"}".replace("ID", incAndGetIdAsString()));
    }

}
