package dev.mouradski.ftso.prices.client.coinw;

import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@ApplicationScoped
public class CoinwTickersEndpoint extends AbstractClientEndpoint {
    @Scheduled(every = "2s")
    public void getTickers() {
        this.lastTickerTime = System.currentTimeMillis();

        if (exchanges.contains(getExchange())) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.coinw.com/api/v1/public?command=returnTicker"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            try {
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());

                var tickerResponse = gson.fromJson(response.body(), TickersResponse.class);

                tickerResponse.getData().entrySet().forEach(e -> {
                    var pair = SymbolHelper.getPair(e.getKey());

                    if (getAssets(true).contains(pair.getLeft()) && getAllQuotesExceptBusd(true).contains(pair.getRight())) {
                        pushTicker(Ticker.builder().source(Source.REST).exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(Double.valueOf(e.getValue().getLast())).timestamp(currentTimestamp()).build());
                    }
                });

            } catch (IOException | InterruptedException e) {
            }
        }
    }

    @Override
    protected String getUri() {
        return null;
    }

    @Override
    protected String getExchange() {
        return "coinw";
    }
}
