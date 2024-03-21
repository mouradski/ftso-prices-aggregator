package dev.mouradski.ftso.prices.client.bit2me;

import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@ApplicationScoped
@Startup
public class Bit2meTickerUpdater extends AbstractClientEndpoint {
    @Override
    protected String getUri() {
        return null;
    }

    @Override
    protected String getExchange() {
        return "bit2me";
    }

    @Scheduled(every = "2s")
    public void getTickers() {
        this.lastTickerTime = System.currentTimeMillis();

        if (exchanges.contains(getExchange()) && this.isCircuitClosed()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://gateway.bit2me.com/v1/currency/ticker"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            var client = HttpClient.newHttpClient();

            Uni.createFrom().completionStage(
                            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                                    .thenApply(HttpResponse::body)
                                    .thenApply(this::mapRate)
                    )
                    .subscribe().with(item -> processCurrencyRates(item), this::catchRestError);
        }
    }

    private Map<String, Map<String, Double>> mapRate(String json) {
        try {
            Map<String, Map<String, Double>> currencyRates = objectMapper.readValue(json, Map.class);
            return currencyRates;
        } catch (Exception e) {
            return null;
        }
    }

    private void processCurrencyRates(Map<String, Map<String, Double>> ratesMap) {
        ratesMap.entrySet().forEach(e -> {
            var quote = e.getKey();
            e.getValue().entrySet().forEach(rates -> {
                var base = rates.getKey();
                if (getAssets(true).contains(base) && getAllQuotesExceptBusd(true).contains(quote)) {
                    var price = Double.valueOf(rates.getValue().toString());

                    this.pushTicker(Ticker.builder().source(Source.REST).exchange(getExchange()).base(base).quote(quote).lastPrice(price).timestamp(currentTimestamp()).build());
                }
            });
        });

    }
}
