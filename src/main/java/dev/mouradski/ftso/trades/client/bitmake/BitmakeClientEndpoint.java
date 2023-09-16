package dev.mouradski.ftso.trades.client.bitmake;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Ticker;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@ClientEndpoint
@Slf4j
@Startup
public class BitmakeClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://ws.bitmake.com/t/v1/ws";
    }

    @Override
    protected String getExchange() {
        return "bitmake";
    }

    @Override
    protected void subscribeTrade() {
        getAssets(true).forEach(base -> {
            this.sendMessage("{\"tp\": \"trade\", \"e\": \"sub\", \"ps\": { \"symbol\": \"BASE_USDT\"}}".replace("BASE", base));
        });

    }

    @Override
    protected void subscribeTicker() {
        getAssets(true).forEach(base -> {
            this.sendMessage("{\"tp\": \"realtimes\", \"e\": \"sub\", \"ps\": { \"symbol\": \"BASE_USDT\"}}".replace("BASE", base));
        });

    }

    @Override
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {
        if (!message.contains("trade")) {
            return Optional.empty();
        }

        var tradePayload = objectMapper.readValue(message, TradePayload.class);

        var pair = SymbolHelper.getPair(tradePayload.getPs().getSymbol());

        var trades = new ArrayList<Trade>();

        tradePayload.getD().forEach(trade -> {
            trades.add(Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).price(trade.getP()).amount(trade.getQ()).timestamp(currentTimestamp()).build());
        });

        return Optional.of(trades);
    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("realtimes")) {
            return Optional.empty();
        }

        var tickerPayload = objectMapper.readValue(message, TickerPayload.class);

        var pair = SymbolHelper.getPair(tickerPayload.getPs().getSymbol());

        var tickers = new ArrayList<Ticker>();

        tickerPayload.getD().forEach(ticker -> {
            tickers.add(Ticker.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).timestamp(currentTimestamp()).lastPrice(ticker.getL()).build());
        });

        return Optional.of(tickers);
    }
}
