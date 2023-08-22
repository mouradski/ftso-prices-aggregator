package dev.mouradski.ftso.trades.client.okex;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Ticker;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ClientEndpoint
@Component
public class OkexClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://ws.okx.com:8443/ws/v5/public";
    }

    @Override
    protected void subscribeTrade() {

        var subscriptionMsg = new StringBuilder("{\"op\": \"subscribe\",\"args\": [");

        var channels = new ArrayList<String>();

        getAssets(true).forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> channels.add("{\"channel\": \"trades\",\"instId\": \"SYMBOL-QUOTE\"}".replace("SYMBOL", base).replace("QUOTE", quote))));

        subscriptionMsg.append(channels.stream().collect(Collectors.joining(",")));
        subscriptionMsg.append("]}");

        this.sendMessage(subscriptionMsg.toString());

    }

    @Override
    protected void subscribeTicker() {

        var subscriptionMsg = new StringBuilder("{\"op\": \"subscribe\",\"args\": [");

        var channels = new ArrayList<String>();

        getAssets(true).forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> channels.add("{\"channel\": \"index-tickers\",\"instId\": \"SYMBOL-QUOTE\"}".replace("SYMBOL", base).replace("QUOTE", quote))));

        subscriptionMsg.append(channels.stream().collect(Collectors.joining(",")));
        subscriptionMsg.append("]}");

        this.sendMessage(subscriptionMsg.toString());

    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("index-tickers")) {
            return Optional.empty();
        }

        var tickerInfo = objectMapper.readValue(message, TickerInfo.class);

        var pair = SymbolHelper.getPair(tickerInfo.getArg().getInstId());

        return Optional.of(Collections.singletonList(Ticker.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(tickerInfo.getData().get(0).getIdxPx()).timestamp(currentTimestamp()).build()));
    }

    @Override
    protected String getExchange() {
        return "okex";
    }

    @Override
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {

        if (!message.contains("\"channel\":\"trades\"")) {
            return Optional.empty();
        }

        var tradeData = objectMapper.readValue(message, TradeData.class);

        var pair = SymbolHelper.getPair(tradeData.getArg().getInstId());

        var trades = new ArrayList<Trade>();

        tradeData.getData().forEach(data -> {
            trades.add(Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).price(data.getPx()).amount(data.getSz()).timestamp(currentTimestamp()).build());
        });

        return Optional.of(trades);
    }

    @Scheduled(fixedDelay = 30 * 1000)
    public void ping() {
        this.sendMessage("ping");
    }
}
