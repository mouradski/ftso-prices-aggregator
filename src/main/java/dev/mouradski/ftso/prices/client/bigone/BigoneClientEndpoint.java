package dev.mouradski.ftso.prices.client.bigone;

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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@ApplicationScoped
@ClientEndpoint
@Startup
public class BigoneClientEndpoint extends AbstractClientEndpoint {
    @Override
    protected String getUri() {
        return null;
    }

    @Override
    protected String getExchange() {
        return "bigone";
    }

    @Scheduled(every = "1s")
    public void getTickers() {
        if (exchanges.contains(getExchange()) && this.isCircuitClosed()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://big.one/api/v3/asset_pairs/tickers"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            try {
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());

                var marketsData = gson.fromJson(response.body(), MarketDataResponse.class).getData();


                for (var ticker : marketsData) {
                    var pair = SymbolHelper.getPair(ticker.getAsset_pair_name());
                    if (getAssets(true).contains(pair.getLeft()) && getAllQuotes(true).contains(pair.getRight())) {
                        pushTicker(Ticker.builder().source(Source.REST).exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(Double.parseDouble(ticker.getClose())).timestamp(currentTimestamp()).build());
                    }
                }

            } catch (IOException | InterruptedException ignored) {
            }

            Uni.createFrom().completionStage(() -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
                    .onItem().transform(response -> gson.fromJson(response.body(), MarketDataResponse.class))
                    .onItem().transformToMulti(marketDataResponse -> Multi.createFrom().item(marketDataResponse))
                    .onFailure().invoke(this::catchRestError)
                    .subscribe().with(tickerData -> {
                        tickerData.getData().forEach(tickerDetail -> {
                            var pair = SymbolHelper.getPair(tickerDetail.getAsset_pair_name());
                            if (getAssets(true).contains(pair.getLeft()) && getAllQuotes(true).contains(pair.getRight())) {
                                pushTicker(Ticker.builder()
                                        .source(Source.REST)
                                        .exchange(getExchange())
                                        .base(pair.getLeft())
                                        .quote(pair.getRight())
                                        .lastPrice(Double.parseDouble(tickerDetail.getClose()))
                                        .timestamp(currentTimestamp())
                                        .build());
                            }
                        });

                    }, this::catchRestError);
        }
    }
}
