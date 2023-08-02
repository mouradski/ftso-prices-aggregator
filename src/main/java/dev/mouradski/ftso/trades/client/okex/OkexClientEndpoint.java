package dev.mouradski.ftso.trades.client.okex;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;

import javax.websocket.ClientEndpoint;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@ClientEndpoint
public class OkexClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://ws.okx.com:8443/ws/v5/public";
    }

    @Override
    protected void subscribe() {

        var subscriptionMsg = new StringBuilder("{\"op\": \"subscribe\",\"args\": [");

        var channels = new ArrayList<String>();

        getAssets(true).forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> channels.add("{\"channel\": \"trades\",\"instId\": \"SYMBOL-QUOTE\"}".replace("SYMBOL", base).replace("QUOTE", quote))));

        subscriptionMsg.append(channels.stream().collect(Collectors.joining(",")));
        subscriptionMsg.append("]}");

        this.sendMessage(subscriptionMsg.toString());

    }

    @Override
    protected String getExchange() {
        return "okex";
    }

    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {

        if (!message.contains("\"channel\":\"trades\"")) {
            return new ArrayList<>();
        }

        var tradeData = objectMapper.readValue(message, TradeData.class);

        var pair = SymbolHelper.getPair(tradeData.getArg().getInstId());

        var trades = new ArrayList<Trade>();

        tradeData.getData().forEach(data -> {
            trades.add(Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).price(data.getPx()).amount(data.getSz()).timestamp(currentTimestamp()).build());
        });

        return trades;
    }

    @Scheduled(every="30s")
    public void ping() {
        this.sendMessage("ping");
    }
}
