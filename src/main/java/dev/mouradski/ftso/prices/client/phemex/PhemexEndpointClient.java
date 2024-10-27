package dev.mouradski.ftso.prices.client.phemex;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
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
public class PhemexEndpointClient extends AbstractClientEndpoint {
    @Override
    protected String getUri() {
        return null;
    }

    @Override
    protected String getExchange() {
        return "phemex";
    }


    @Scheduled(every = "1s")
    public void getTickers() {
        this.messageReceived();
        if (exchanges.contains(getExchange()) && this.isCircuitClosed()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.phemex.com/md/spot/ticker/24hr/all"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            Uni.createFrom().completionStage(() -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
                    .onItem().transform(response -> {
                        try {
                            return objectMapper.readValue(response.body(), TickersResponses.class);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .onItem().transformToMulti(tickerResponse -> Multi.createFrom().iterable(tickerResponse.getResult()))
                    .onFailure().invoke(this::catchRestError)
                    .subscribe().with(tickerEntry -> {
                        if (tickerEntry.getSymbol()!= null && tickerEntry.getLastPrice() != null) {
                            var pair = SymbolHelper.getPair(tickerEntry.getSymbol().replace("s", ""));
                            if (getAssets(true).contains(pair.getLeft()) && getAllQuotes(true).contains(pair.getRight())) {
                                pushTicker(Ticker.builder()
                                        .source(Source.REST)
                                        .exchange(getExchange())
                                        .base(pair.getLeft())
                                        .quote(pair.getRight())
                                        .lastPrice(tickerEntry.getLastPrice().doubleValue() / 100000000)
                                        .timestamp(currentTimestamp())
                                        .build());
                            }
                        }
                    }, this::catchRestError);
        }
    }
}
