package dev.mouradski.ftso.prices.client.bitso;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

@ApplicationScoped
public class BitsoTickerUpdater extends AbstractClientEndpoint {
    @Override
    protected String getUri() {
        return null;
    }

    @Override
    protected String getExchange() {
        return "bitso";
    }

    @Scheduled(every = "2s")
    public void getTickers() {

        if (!exchanges.contains(getExchange()) || !this.isCircuitClosed()) {
            return;
        }

        this.lastTickerTime = System.currentTimeMillis();

        Arrays.asList("https://bitso.com/api/v3/ticker").forEach(url -> {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            Uni.createFrom().completionStage(() -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
                    .onItem().transform(response -> {
                        try {
                            return objectMapper.readValue(response.body(), Market.class);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .subscribe().with(marketsData -> {

                        marketsData.getPayload().forEach(marketItem -> {
                            var pair = SymbolHelper.getPair(marketItem.getBook());
                            if (getAssets(true).contains(pair.getKey()) && getAllQuotes(true).contains(pair.getValue())) {
                                pushTicker(Ticker.builder().source(Source.REST).exchange(getExchange()).base(pair.getKey()).quote(pair.getValue()).timestamp(currentTimestamp()).lastPrice(Double.valueOf(marketItem.getLast())).build());
                            }
                        });
                    }, this::catchRestError);
        });
    }
}
