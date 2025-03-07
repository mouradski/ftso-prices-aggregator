package dev.mouradski.ftso.prices.client.bluebit;

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

@ApplicationScoped
@ClientEndpoint
@Startup
public class BluebitClientEndpoint extends AbstractClientEndpoint {
    @Override
    protected String getUri() {
        return null;
    }

    @Override
    protected String getExchange() {
        return "bluebit";
    }


    @Scheduled(every = "1s")
    public void getTickers() {

        if (exchanges.contains(getExchange()) && this.isCircuitClosed()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://openapi.bluebit.io/open/api/get_allticker"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            Uni.createFrom().completionStage(() -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
                    .onItem().transform(response -> gson.fromJson(response.body(), ApiResponse.class))
                    .onItem().transformToMulti(tickersResponse -> Multi.createFrom().iterable(tickersResponse.getData().getTicker()))
                    .onFailure().invoke(this::catchRestError)
                    .subscribe().with(entry -> {
                        var pair = SymbolHelper.getPair(entry.getSymbol());
                        if (getAssets(true).contains(pair.getLeft()) && getAllQuotes(true).contains(pair.getRight())) {
                            pushTicker(Ticker.builder()
                                    .source(Source.REST)
                                    .exchange(getExchange())
                                    .base(pair.getLeft())
                                    .quote(pair.getRight())
                                    .lastPrice(entry.getLast())
                                    .timestamp(currentTimestamp())
                                    .build());
                        }
                    }, this::catchRestError);
        }
    }

}
