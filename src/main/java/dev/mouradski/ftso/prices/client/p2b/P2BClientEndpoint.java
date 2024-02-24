package dev.mouradski.ftso.prices.client.p2b;


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
public class P2BClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return null;
    }

    @Scheduled(every = "1s")
    public void getTickers() {
        this.lastTickerTime = System.currentTimeMillis();

        if (exchanges.contains(getExchange())) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.p2pb2b.com/api/v2/public/tickers"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            Uni.createFrom().completionStage(() -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
                    .onItem().transform(response -> gson.fromJson(response.body(), TickerApiResponse.class))
                    .onItem().transformToUni(tickerResponse -> Uni.createFrom().item(tickerResponse))
                    .onItem().transformToMulti(tickerResponse -> Multi.createFrom().iterable(tickerResponse.getResult().entrySet()))
                    .subscribe().with(entry -> {
                        var key = entry.getKey();
                        var value = entry.getValue();
                        var pair = SymbolHelper.getPair(key);

                        if (getAssets(true).contains(pair.getLeft()) && getAllQuotesExceptBusd(true).contains(pair.getRight())) {
                            pushTicker(Ticker.builder()
                                    .source(Source.REST)
                                    .exchange(getExchange())
                                    .base(pair.getLeft())
                                    .quote(pair.getRight())
                                    .lastPrice(value.getTicker().getLast())
                                    .timestamp(currentTimestamp())
                                    .build());
                        }
                    }, failure -> {});
        }
    }

    @Override
    protected String getExchange() {
        return "p2b";
    }

    @Scheduled(every = "30s")
    public void ping() {
        this.sendMessage("{\"method\":\"server.ping\",\"params\":[],\"id\":ID}".replace("ID", incAndGetIdAsString()));
    }
}
