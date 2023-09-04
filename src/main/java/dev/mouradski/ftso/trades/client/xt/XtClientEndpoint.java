package dev.mouradski.ftso.trades.client.xt;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Ticker;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@ClientEndpoint
@Startup
public class XtClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://stream.xt.com/public";
    }

    @Override
    protected void subscribeTrade() {
        var pairs = new ArrayList<String>();

        getAssets(false).forEach(base -> getAllQuotesExceptBusd(false).forEach(quote -> {
            pairs.add("\"trade@" + base + "_" + quote + "\"");
        }));

        this.sendMessage("{     \"method\": \"subscribe\",      \"params\": [PAIRS],      \"id\": \"ID\" }"
                .replace("ID", incAndGetIdAsString())
                .replace("PAIRS", String.join(",", pairs)));
    }

    @Override
    protected void subscribeTicker() {
        this.sendMessage("{     \"method\": \"subscribe\",  \"params\": [\"tickers\"]}");
    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("tickers")) {
            return Optional.empty();
        }

        var tickers = new ArrayList<Ticker>();


        var tickerEvent = this.objectMapper.readValue(message, TickerEvent.class);



        tickerEvent.getData().forEach(ticker -> {
            var pair = SymbolHelper.getPair(ticker.getSymbol());

            if (getAssets(false).contains(pair.getLeft().toLowerCase()) && getAllQuotes(false).contains(pair.getRight().toLowerCase())) {
                tickers.add(Ticker.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(Double.valueOf(ticker.getClose())).timestamp(currentTimestamp()).build());
            }
        });

        return Optional.of(tickers);

    }

    @Override
    protected String getExchange() {
        return "xt";
    }


    @Override
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {

        if (!message.contains("\"topic\":\"trade\"")) {
            return Optional.empty();
        }

        var eventData = this.objectMapper.readValue(message, EventData.class);

        var pair = SymbolHelper.getPair(eventData.getEvent().replace("trade@", ""));

        return Optional.of(Collections.singletonList(Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).price(eventData.getData().getPrice()).amount(eventData.getData().getQuantity()).timestamp(currentTimestamp()).build()));
    }

    @Scheduled(every="20s")
    public void ping() {
        this.sendMessage("ping");
    }
}
