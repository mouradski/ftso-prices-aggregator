package dev.mouradski.ftso.trades.client.xt;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.client.HttpTickers;
import dev.mouradski.ftso.trades.client.bybit.TickerResponse;
import dev.mouradski.ftso.trades.model.Ticker;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@ClientEndpoint
@Startup
public class XtClientEndpoint extends AbstractClientEndpoint implements HttpTickers {

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
    public void updateTickers() {
        this.lastTickerTime = System.currentTimeMillis();
        if (subscribeTicker && exchanges.contains(getExchange())) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://sapi.xt.com/v4/public/ticker"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            try {
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());

                var apiResponse = gson.fromJson(response.body(), ApiResponse.class);

                apiResponse.getResult().forEach(ticker -> {
                    var pair = SymbolHelper.getPair(ticker.getS());

                    if (getAssets(true).contains(pair.getLeft()) && getAllQuotesExceptBusd(true).contains(pair.getRight())) {
                        pushTicker(Ticker.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(Double.valueOf(ticker.getC())).timestamp(currentTimestamp()).build());
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
