package dev.mouradski.ftso.prices.client.cexio;

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
public class CexioClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return null;
    }

    @Override
    protected String getExchange() {
        return "cexio";
    }

    @Scheduled(every = "2s")
    public void getTickers() {
        if (exchanges.contains(getExchange()) && this.isCircuitClosed()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://trade.cex.io/api/spot/rest-public/get_ticker"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            Uni.createFrom().completionStage(() -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
                    .onItem().transform(response -> gson.fromJson(response.body(), TickerResponse.class))
                    .onItem().transformToMulti(tickerResponse -> Multi.createFrom().iterable(tickerResponse.getData().entrySet()))
                    .onFailure().invoke(this::catchRestError)
                    .subscribe().with(entry -> {
                        var pair = SymbolHelper.getPair(entry.getKey());
                        var ticker = entry.getValue();
                        if (getAssets(true).contains(pair.getLeft()) && getAllQuotes(true).contains(pair.getRight())) {
                            pushTicker(Ticker.builder()
                                    .source(Source.REST)
                                    .exchange(getExchange())
                                    .base(pair.getLeft())
                                    .quote(pair.getRight())
                                    .lastPrice(Double.parseDouble(ticker.getLast()))
                                    .timestamp(currentTimestamp())
                                    .build());
                        }
                    }, this::catchRestError);
        }
    }
}
