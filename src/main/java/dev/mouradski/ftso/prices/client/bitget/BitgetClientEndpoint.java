package dev.mouradski.ftso.prices.client.bitget;

import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;
import jakarta.websocket.ClientEndpoint;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Singleton
@ClientEndpoint
@Startup
public class BitgetClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return null;
    }

    @Scheduled(every = "1s")
    public void fetchTickers() {
        if (exchanges.contains(getExchange()) && this.isCircuitClosed()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.bitget.com/api/v2/spot/market/tickers"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            Uni.createFrom().completionStage(() -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
                    .onItem().transform(response -> gson.fromJson(response.body(), TickerMessage.class))
                    .onItem().transformToMulti(tickersResponse -> Multi.createFrom().items(tickersResponse.getData()))
                    .subscribe().with(data -> {
                        data.forEach(ticker -> {
                            var pair = SymbolHelper.getPair(ticker.getSymbol());
                            if (getAssets(true).contains(pair.getLeft()) && getAllQuotes(true).contains(pair.getRight())) {
                                pushTicker(Ticker.builder()
                                        .source(Source.REST)
                                        .exchange(getExchange())
                                        .base(pair.getLeft())
                                        .quote(pair.getRight())
                                        .lastPrice(Double.parseDouble(ticker.getLastPr()))
                                        .timestamp(currentTimestamp())
                                        .build());
                            }
                        });
                    },this::catchRestError);
        }

    }

    @Override
    protected String getExchange() {
        return "bitget";
    }
}
