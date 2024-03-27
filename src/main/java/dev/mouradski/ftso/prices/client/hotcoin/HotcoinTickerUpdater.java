package dev.mouradski.ftso.prices.client.hotcoin;

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
public class HotcoinTickerUpdater extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return null;
    }

    @Override
    protected String getExchange() {
        return "hotcoin";
    }

    @Scheduled(every = "1s")
    public void getTickers() {
        this.lastTickerTime = System.currentTimeMillis();

        if (exchanges.contains(getExchange()) && this.isCircuitClosed()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.hotcoinfin.com/v1/market/ticker"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            Uni.createFrom().completionStage(() -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
                    .onItem().transform(response -> gson.fromJson(response.body(), Tickers.class))
                    .onItem().transformToMulti(tickersResponse -> Multi.createFrom().items(tickersResponse.getTicker()))
                    .subscribe().with(data -> {
                        var pair = SymbolHelper.getPair(data.getSymbol());
                        if (getAssets(true).contains(pair.getLeft()) && getAllQuotes(true).contains(pair.getRight())) {
                            var ticker = Ticker.builder()
                                    .source(Source.REST)
                                    .exchange(getExchange())
                                    .base(pair.getLeft())
                                    .quote(pair.getRight())
                                    .lastPrice(data.getLast())
                                    .timestamp(currentTimestamp())
                                    .build();
                            pushTicker(ticker);
                        }
                    }, this::catchRestError);
        }
    }
}
