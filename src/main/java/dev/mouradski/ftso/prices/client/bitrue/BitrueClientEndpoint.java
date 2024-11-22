package dev.mouradski.ftso.prices.client.bitrue;

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
import jakarta.websocket.ClientEndpoint;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

@ApplicationScoped
@ClientEndpoint
@Startup
public class BitrueClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return null;
    }

    @Override
    protected String getExchange() {
        return "bitrue";
    }

    @Scheduled(every = "1s")
    public void getTickers() {
        this.messageReceived();

        if (exchanges.contains(getExchange()) && this.isCircuitClosed()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.bitrue.com/api/v1/ticker/24hr"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            Uni.createFrom().completionStage(() -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
                    .onFailure().invoke(this::catchRestError)
                    .onItem().transform(response -> {
                        try {
                            return objectMapper.readValue(response.body(), TickerData[].class);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .onItem().transformToMulti(tickers -> Multi.createFrom().iterable(Arrays.stream(tickers).toList()))
                    .subscribe().with(ticker -> {
                        var pair = SymbolHelper.getPair(ticker.getSymbol());

                        if (getAssets(true).contains(pair.getLeft()) && getAllQuotes(true).contains(pair.getRight())) {
                            pushTicker(Ticker.builder()
                                    .source(Source.REST)
                                    .exchange(getExchange())
                                    .base(pair.getLeft())
                                    .quote(pair.getRight())
                                    .lastPrice(ticker.getLastPrice())
                                    .timestamp(currentTimestamp())
                                    .build());
                        }
                    }, this::catchRestError);
        }
    }
}
