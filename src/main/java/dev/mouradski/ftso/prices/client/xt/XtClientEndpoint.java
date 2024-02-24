package dev.mouradski.ftso.prices.client.xt;

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
public class XtClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return null;
    }

    @Override
    protected void subscribeTicker() {
        this.sendMessage("{     \"method\": \"subscribe\",  \"params\": [\"tickers\"]}");
    }

    @Scheduled(every = "1s")
    public void fetchTickers() {
        fetchTickers(false);
    }

    private void fetchTickers(boolean future) {
        var url = future ? "https://fapi.xt.com/future/market/v1/public/q/tickers" :
                "https://sapi.xt.com/v4/public/ticker";

        this.lastTickerTime = System.currentTimeMillis();
        if (exchanges.contains(getExchange())) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            Uni.createFrom().completionStage(() -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
                    .onItem().transform(response -> gson.fromJson(response.body(), TickerResponse.class))
                    .onItem().transformToMulti(tickerResponse -> Multi.createFrom().iterable(tickerResponse.getResult()))
                    .subscribe().with(ticker -> {
                        var pair = SymbolHelper.getPair(ticker.getS());
                        if (getAssets(true).contains(pair.getLeft()) && getAllQuotesExceptBusd(true).contains(pair.getRight())) {
                            pushTicker(Ticker.builder()
                                    .source(Source.REST)
                                    .exchange(getExchange() + (future ? "future" : ""))
                                    .base(pair.getLeft())
                                    .quote(pair.getRight())
                                    .lastPrice(Double.parseDouble(ticker.getC()))
                                    .timestamp(currentTimestamp())
                                    .build());
                        }
                    }, failure -> {
                    });
        }
    }

    @Override
    protected String getExchange() {
        return "xt";
    }
}
