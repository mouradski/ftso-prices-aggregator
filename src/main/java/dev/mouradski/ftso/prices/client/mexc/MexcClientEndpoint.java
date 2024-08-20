package dev.mouradski.ftso.prices.client.mexc;

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
public class MexcClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return null;
    }

    @Override
    protected void subscribeTicker() {
        getAssets(true).forEach(base -> getAllQuotes(true).forEach(quote -> this.sendMessage("{\"op\":\"sub.ticker\", \"symbol\":\"SYMBOL_QUOTE\"}".replace("SYMBOL", base).replace("QUOTE", quote))));
    }

    @Override
    protected String getExchange() {
        return "mexc";
    }

    @Scheduled(every = "1s")
    public void getTickers() {
        this.messageReceived();

        if (exchanges.contains(getExchange()) && this.isCircuitClosed()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.mexc.com/api/v3/ticker/price"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            Uni.createFrom().completionStage(() -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
                    .onItem().transform(response -> gson.fromJson(response.body(), PriceTicker[].class))
                    .onItem().transformToMulti(tickers -> Multi.createFrom().items(tickers))
                    .onFailure().invoke(this::catchRestError)
                    .subscribe().with(ticker -> {
                        var pair = SymbolHelper.getPair(ticker.getSymbol());
                        if (getAssets(true).contains(pair.getLeft()) && getAllQuotes(true).contains(pair.getRight())) {
                            pushTicker(Ticker.builder()
                                    .source(Source.REST)
                                    .exchange(getExchange())
                                    .base(pair.getLeft())
                                    .quote(pair.getRight())
                                    .lastPrice(ticker.getPrice())
                                    .timestamp(currentTimestamp())
                                    .build());
                        }
                    }, this::catchRestError);


            var futureRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://contract.mexc.com/api/v1/contract/ticker"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();


            Uni.createFrom().completionStage(() -> client.sendAsync(futureRequest, HttpResponse.BodyHandlers.ofString()))
                    .onItem().transform(response -> gson.fromJson(response.body(), Contracts.class))
                    .onItem().transformToMulti(contracts -> Multi.createFrom().items(contracts.getData()))
                    .onFailure().invoke(this::catchRestError)
                    .subscribe().with(contractData -> {
                        contractData.forEach(contract -> {
                            var pair = SymbolHelper.getPair(contract.getSymbol());
                            if (getAssets(true).contains(pair.getLeft()) && getAllQuotes(true).contains(pair.getRight())) {
                                pushTicker(Ticker.builder()
                                        .source(Source.REST)
                                        .exchange(getExchange() + "future")
                                        .base(pair.getLeft())
                                        .quote(pair.getRight())
                                        .lastPrice(contract.getLastPrice())
                                        .timestamp(currentTimestamp())
                                        .build());
                            }
                        });

                    }, this::catchRestError);
        }


    }
}
