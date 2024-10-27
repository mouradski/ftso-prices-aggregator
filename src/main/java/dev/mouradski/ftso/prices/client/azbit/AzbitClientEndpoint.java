package dev.mouradski.ftso.prices.client.azbit;

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
import java.util.Arrays;

@ApplicationScoped
@Startup
public class AzbitClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected void subscribeTicker() {
        super.subscribeTicker();
    }

    @Override
    protected String getExchange() {
        return "azbit";
    }

    @Override
    protected String getUri() {
        return null;
    }

    @Scheduled(every = "3s")
    public void getTickers() {
        this.messageReceived();
        if (exchanges.contains(getExchange()) && this.isCircuitClosed()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://data.azbit.com/api/tickers"))
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
                    .onItem().transformToMulti(tickerResponse -> Multi.createFrom().iterable(Arrays.stream(tickerResponse).toList()))
                    .onFailure().invoke(this::catchRestError)
                    .subscribe().with(tickerEntry -> {
                        if (tickerEntry.getSymbol() != null && tickerEntry.getPrice() != null && tickerEntry.getPrice() != 0) {
                            var pair = SymbolHelper.getPair(tickerEntry.getSymbol());
                            if (getAssets(true).contains(pair.getLeft()) && getAllQuotes(true).contains(pair.getRight())) {
                                pushTicker(Ticker.builder()
                                        .source(Source.REST)
                                        .exchange(getExchange())
                                        .base(pair.getLeft())
                                        .quote(pair.getRight())
                                        .lastPrice(tickerEntry.getPrice())
                                        .timestamp(currentTimestamp())
                                        .build());
                            }
                        }
                    }, this::catchRestError);
        }
    }
}
