package dev.mouradski.ftso.prices.client.bitpanda;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.Map;

@ApplicationScoped
@Slf4j
@Startup
public class BitpandaClientEndpoint extends AbstractClientEndpoint {
    @Override
    protected String getUri() {
        return null;
    }

    @Override
    protected String getExchange() {
        return "bitpanda";
    }


    @Scheduled(every = "1s")
    public void getTickers() {
        if (exchanges.contains(getExchange()) && this.isCircuitClosed()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.bitpanda.com/v1/ticker"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();


            Uni.createFrom().completionStage(() -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
                    .onItem().transform(response -> Collections.singletonList(response.body()))
                    .onFailure().invoke(this::catchRestError)
                    .subscribe().with(entry -> {
                        try {
                            var currencyMap = objectMapper.readValue(entry.get(0), new TypeReference<Map<String, Map<String, String>>>() {});

                            currencyMap.entrySet().forEach(currencyEntry -> {
                                var base = currencyEntry.getKey();

                                if (getAssets(true).contains(base)) {
                                    currencyEntry.getValue().entrySet().forEach(quoteEntry -> {
                                        var quote = quoteEntry.getKey();

                                        if (getAllQuotes(true).contains(quote)) {
                                            pushTicker(Ticker.builder()
                                                    .source(Source.REST)
                                                    .exchange(getExchange())
                                                    .base(base)
                                                    .quote(quote)
                                                    .lastPrice(Double.parseDouble(quoteEntry.getValue()))
                                                    .timestamp(currentTimestamp())
                                                    .build());
                                        }
                                    });
                                }
                            });
                        } catch (JsonProcessingException e) {
                        }

                    }, this::catchRestError);
        }
    }
}
