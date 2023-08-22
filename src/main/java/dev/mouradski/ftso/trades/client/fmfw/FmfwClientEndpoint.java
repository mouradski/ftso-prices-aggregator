package dev.mouradski.ftso.trades.client.fmfw;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Ticker;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ClientEndpoint
@Component
public class FmfwClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://api.fmfw.io/api/3/ws/public";
    }

    @Override
    protected void subscribeTrade() {

        var pairs = new ArrayList<String>();

        getAssets().stream().map(String::toUpperCase).forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> pairs.add("\"" + base + quote + "\"")));

        this.sendMessage("{\"method\":\"subscribe\", \"ch\":\"trades\", \"params\":{\"symbols\": [PAIRS]}, \"id\": ID}".replace("ID", incAndGetIdAsString()).replace("PAIRS", String.join(",", pairs)));
    }

    @Override
    protected void subscribeTicker() {

        var pairs = new ArrayList<String>();

        getAssets().stream().map(String::toUpperCase).forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> pairs.add("\"" + base + quote + "\"")));

        this.sendMessage("{\"method\":\"subscribe\", \"ch\":\"ticker/price/1s\", \"params\":{\"symbols\": [PAIRS]}, \"id\": ID}".replace("ID", incAndGetIdAsString()).replace("PAIRS", String.join(",", pairs)));
    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("ticker/price/1s")) {
            return Optional.empty();
        }

        var tickerResponse = objectMapper.readValue(message, TickerResponse.class);

        var tickers = new ArrayList<Ticker>();

        tickerResponse.getData().entrySet().forEach(data -> {
            var pair = SymbolHelper.getPair(data.getKey());
            tickers.add(Ticker.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(data.getValue().getClosePrice()).timestamp(currentTimestamp()).build());
        });

        return Optional.of(tickers);
    }

    @Override
    protected String getExchange() {
        return "fmfw";
    }

    @Override
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {
        if (!message.contains("\"p\"") || !message.contains("trade") || !message.contains("update")) {
            return Optional.empty();
        }

        var fmfwTradeResponse = objectMapper.readValue(message, dev.mouradski.ftso.trades.client.fmfw.FmfwTradeResponse.class);

        var trades = new ArrayList<Trade>();

        fmfwTradeResponse.getUpdate().getTrades().entrySet().forEach(e -> {
            var pair = SymbolHelper.getPair(e.getKey());

            e.getValue().stream().sorted(Comparator.comparing(FmfwTradeResponse.Trade::getT)).forEach(trade -> {
                trades.add(Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).price(trade.getP()).amount(trade.getQ()).timestamp(currentTimestamp()).build());
            });
        });

        return Optional.of(trades);
    }
}
