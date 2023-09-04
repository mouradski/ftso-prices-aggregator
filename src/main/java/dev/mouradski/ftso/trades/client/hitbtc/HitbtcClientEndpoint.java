package dev.mouradski.ftso.trades.client.hitbtc;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Ticker;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@ClientEndpoint
@Startup
public class HitbtcClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://api.hitbtc.com/api/3/ws/public";
    }


    @Override
    protected void subscribeTrade() {
        var pairs = new ArrayList<String>();

        getAssets().stream().map(String::toUpperCase).forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> pairs.add("\"" + base + quote + "\"")));

        this.sendMessage("{\"method\": \"subscribe\",\"ch\": \"trades\",\"params\": {\"symbols\": [PAIRS],\"limit\": 1},\"id\": ID}".replace("PAIRS", pairs.stream().collect(Collectors.joining(","))).replace("ID", new Date().getTime() + ""));
    }

    @Override
    protected void subscribeTicker() {
        var pairs = new ArrayList<String>();

        getAssets().stream().map(String::toUpperCase).forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> pairs.add("\"" + base + quote + "\"")));

        this.sendMessage("{\"method\": \"subscribe\",\"ch\": \"ticker/price/1s\",\"params\": {\"symbols\": [PAIRS],\"limit\": 1},\"id\": ID}".replace("PAIRS", pairs.stream().collect(Collectors.joining(","))).replace("ID", new Date().getTime() + ""));
    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("ticker/price/1s")) {
            return Optional.empty();
        }

        var tickerResponse = objectMapper.readValue(message, TickerResponse.class);

        var tickers = new ArrayList<Ticker>();

        for (Map.Entry<String, TickerResponse.TickerData> entry : tickerResponse.getData().entrySet()) {
            var symbolId = entry.getKey();
            var pair = SymbolHelper.getPair(symbolId);
            Double lastPrice = Double.valueOf(entry.getValue().getC());

            tickers.add(Ticker.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(lastPrice).timestamp(currentTimestamp()).build());

        }
        return Optional.of(tickers);
    }

    @Override
    protected String getExchange() {
        return "hitbtc";
    }

    @Override
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {
        var trades = new ArrayList<Trade>();

        var response = gson.fromJson(message, Response.class);

        if (response.getSnapshot() == null) {
            return Optional.empty();
        }

        response.getSnapshot().entrySet().forEach(e -> {
            var pair = SymbolHelper.getPair(e.getKey());

            for (HitbtcTrade hitbtcTrade : e.getValue()) {
                trades.add(Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).price(hitbtcTrade.getP()).amount(hitbtcTrade.getQ()).build());
            }
        });

        response.getUpdate().entrySet().forEach(e -> {
            var pair = SymbolHelper.getPair(e.getKey());


            for (HitbtcTrade hitbtcTrade : e.getValue()) {
                trades.add(Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).price(hitbtcTrade.getP()).amount(hitbtcTrade.getQ()).timestamp(currentTimestamp()).build());
            }
        });

        return Optional.of(trades);
    }

    @Override
    protected boolean pong(String message) {
        return message.contains("ping");
    }
}
