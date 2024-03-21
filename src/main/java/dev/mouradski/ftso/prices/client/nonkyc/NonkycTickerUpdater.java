package dev.mouradski.ftso.prices.client.nonkyc;


import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

@ApplicationScoped
public class NonkycTickerUpdater extends AbstractClientEndpoint {

    @Scheduled(every = "1s")
    public void getTickers() {

        if (!exchanges.contains(getExchange()) || !this.isCircuitClosed()) {
            return;
        }

        this.lastTickerTime = System.currentTimeMillis();

        Arrays.asList("https://api.nonkyc.io/api/v2/tickers").forEach(url -> {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            Uni.createFrom().completionStage(() -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
                    .onItem().transform(response -> {
                        try {
                            return objectMapper.readValue(response.body(), TickerData[].class);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .onItem().transformToMulti(tickersResponse -> Multi.createFrom().iterable(Arrays.stream(tickersResponse).toList()))
                    .subscribe().with(ticker -> {
                        if (getAssets(true).contains(ticker.getBase()) && getAllQuotesExceptBusd(true).contains(ticker.getQuote())) {
                            pushTicker(Ticker.builder()
                                    .source(Source.REST)
                                    .exchange(getExchange())
                                    .base(ticker.getBase())
                                    .quote(ticker.getQuote())
                                    .lastPrice(Double.parseDouble(ticker.getLastPrice()))
                                    .timestamp(currentTimestamp())
                                    .build());
                        }
                    }, this::catchRestError);
        });
    }

    @Override
    protected String getUri() {
        return null;
    }

    @Override
    protected String getExchange() {
        return "nonkyc";
    }
}
