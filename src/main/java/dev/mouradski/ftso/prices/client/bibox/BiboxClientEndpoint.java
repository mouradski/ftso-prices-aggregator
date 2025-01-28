package dev.mouradski.ftso.prices.client.bibox;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


@ApplicationScoped
@Startup
public class BiboxClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return null;
    }

    @Override
    protected String getExchange() {
        return "bibox";
    }

    @Scheduled(every = "1s")
    public void getTickers() {
        this.messageReceived();

        if (exchanges.contains(getExchange()) && this.isCircuitClosed()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.bibox.com/v3/mdata/marketAll"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();


            Uni.createFrom().completionStage(() -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
                    .onItem().transform(response -> {
                        try {
                            return objectMapper.readValue(response.body(), Tickers.class);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .onItem().transformToMulti(marketDataResponse -> Multi.createFrom().item(marketDataResponse))
                    .onFailure().invoke(this::catchRestError)
                    .subscribe().with(tickerData -> {
                        tickerData.result.forEach(ticker -> {

                            if (getAssets(true).contains(ticker.getBase()) && getAllQuotes(true).contains(ticker.getQuoute())) {
                                pushTicker(Ticker.builder()
                                        .source(Source.REST)
                                        .exchange(getExchange())
                                        .base(ticker.getBase())
                                        .quote(ticker.getQuoute())
                                        .lastPrice(Double.parseDouble(ticker.getLast()))
                                        .timestamp(currentTimestamp())
                                        .build());
                            }
                        });

                    }, this::catchRestError);
        }
    }
}
