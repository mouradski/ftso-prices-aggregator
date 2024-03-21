package dev.mouradski.ftso.prices.client.deepcoin;

import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

@ApplicationScoped
public class DeepcoinTickersEndpoint extends AbstractClientEndpoint {

    @Scheduled(every = "1s")
    public void getTickers() {

        if (!exchanges.contains(getExchange()) || !this.isCircuitClosed()) {
            return;
        }

        this.lastTickerTime = System.currentTimeMillis();

        Arrays.asList("https://api.deepcoin.com/deepcoin/market/tickers?instType=SPOT").forEach(url -> {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            Uni.createFrom().completionStage(() -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
                    .onItem().transform(response -> gson.fromJson(response.body(), TickersResponse.class))
                    .onItem().transformToMulti(tickersResponse -> Multi.createFrom().iterable(tickersResponse.getData()))
                    .subscribe().with(ticker -> {
                        var pair = SymbolHelper.getPair(ticker.getInstId().replace("-SWAP", ""));
                        if (getAssets(true).contains(pair.getLeft()) && getAllQuotesExceptBusd(true).contains(pair.getRight())) {
                            pushTicker(Ticker.builder()
                                    .source(Source.REST)
                                    .exchange("SWAP".equals(ticker.getInstType()) ? (getExchange() + "swap") : getExchange())
                                    .base(pair.getLeft())
                                    .quote(pair.getRight())
                                    .lastPrice(Double.parseDouble(ticker.getLast()))
                                    .timestamp(currentTimestamp())
                                    .build());
                        }
                    }, this::catchRestError);
        });


    }

    @Override
    protected String getUri() {
        return null;
    }

    @Override
    protected String getExchange() {
        return "deepcoin";
    }
}
