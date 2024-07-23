package dev.mouradski.ftso.prices.client.ace;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.client.nonkyc.TickerData;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Map;

@ApplicationScoped
public class AceTickerUpdater extends AbstractClientEndpoint {
    @Override
    protected String getUri() {
        return null;
    }

    @Override
    protected String getExchange() {
        return "ace";
    }

    @Scheduled(every = "2s")
    public void getTickers() {

        if (!exchanges.contains(getExchange()) || !this.isCircuitClosed()) {
            return;
        }

        this.lastTickerTime = System.currentTimeMillis();

        Arrays.asList("https://ace.io/polarisex/oapi/v2/list/tradePrice").forEach(url -> {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            Uni.createFrom().completionStage(() -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
                    .onFailure().invoke(this::catchRestError)
                    .onItem().transform(response -> {
                        try {
                            return (Map<String, MarketData>) objectMapper.readValue(response.body(), objectMapper.getTypeFactory().constructMapType(Map.class, String.class, MarketData.class));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .subscribe().with(marketsData -> {
                        marketsData.entrySet().forEach(entry -> {
                            var pair = SymbolHelper.getPair(entry.getKey());
                            if (getAssets(true).contains(pair.getKey()) && getAllQuotes(true).contains(pair.getValue())) {
                                pushTicker(Ticker.builder().source(Source.REST).exchange(getExchange()).base(pair.getKey()).quote(pair.getValue()).timestamp(currentTimestamp()).lastPrice(Double.valueOf(entry.getValue().getLastPrice())).build());
                            }
                        });
                    }, this::catchRestError);
        });
    }
}
