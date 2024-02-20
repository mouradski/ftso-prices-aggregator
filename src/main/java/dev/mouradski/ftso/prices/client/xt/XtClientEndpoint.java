package dev.mouradski.ftso.prices.client.xt;

import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@ApplicationScoped
@ClientEndpoint
@Startup
public class XtClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://stream.xt.com/public";
    }

    @Override
    protected void subscribeTicker() {
        this.sendMessage("{     \"method\": \"subscribe\",  \"params\": [\"tickers\"]}");
    }

    @Scheduled(every = "2s")
    public void fetchTickers() {
        fetchTickers(true);
        fetchTickers(false);
    }

    private void fetchTickers(boolean future) {
        var url = future ? "https://fapi.xt.com/future/market/v1/public/q/tickers" :
                "https://sapi.xt.com/v4/public/ticker";

        this.lastTickerTime = System.currentTimeMillis();
        if (exchanges.contains(getExchange())) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            try {
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());

                var tickerResponse = gson.fromJson(response.body(), TickerResponse.class);

                tickerResponse.getResult().forEach(ticker -> {
                    var pair = SymbolHelper.getPair(ticker.getS());

                    if (getAssets(true).contains(pair.getLeft()) && getAllQuotesExceptBusd(true).contains(pair.getRight())) {
                        pushTicker(Ticker.builder().exchange(getExchange() + (future ? "future" : "")).base(pair.getLeft()).quote(pair.getRight()).lastPrice(Double.valueOf(ticker.getC())).timestamp(currentTimestamp()).build());
                    }
                });

            } catch (IOException | InterruptedException ignored) {
            }
        }
    }

    @Override
    protected String getExchange() {
        return "xt";
    }

    @Scheduled(every="20s")
    public void ping() {
        this.sendMessage("ping");
    }


    @Override
    protected boolean httpTicker() {
        return true;
    }
}
