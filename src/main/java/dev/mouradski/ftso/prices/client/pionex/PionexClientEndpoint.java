package dev.mouradski.ftso.prices.client.pionex;

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
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
@ClientEndpoint
@Slf4j
@Startup
public class PionexClientEndpoint extends AbstractClientEndpoint {

    protected Set<String> supportedSymbols;

    @Override
    protected String getUri() {
        return "wss://ws.pionex.com/wsPub";
    }

    @Override
    protected String getExchange() {
        return "pionex";
    }

    @Override
    protected boolean pong(String message) {
        if (message.contains("PING")) {
            this.sendMessage("{\"op\": \"PONG\", \"timestamp\": TIME}".replace("TIME", new Date().getTime() + ""));
            return true;
        }

        return false;
    }

    @Scheduled(every = "1s")
    public void getTickers() {
        this.lastTickerTime = System.currentTimeMillis();

        if (exchanges.contains(getExchange()) && this.isCircuitClosed()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.pionex.com/api/v1/market/tickers"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            Uni.createFrom().completionStage(() -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
                    .onItem().transform(response -> gson.fromJson(response.body(), TickerData.class))
                    .onItem().transformToMulti(tickerData -> Multi.createFrom().iterable(tickerData.getData().getTickers()))
                    .subscribe().with(ticker -> {
                        var pair = SymbolHelper.getPair(ticker.getSymbol());
                        if (getAssets(true).contains(pair.getLeft()) && getAllQuotes(true).contains(pair.getRight())) {
                            pushTicker(Ticker.builder()
                                    .source(Source.REST)
                                    .exchange(getExchange())
                                    .base(pair.getLeft())
                                    .quote(pair.getRight())
                                    .lastPrice(ticker.getClose())
                                    .timestamp(currentTimestamp())
                                    .build());
                        }
                    }, this::catchRestError);
        }
    }

    @Override
    protected void prepareConnection() {
        var client = HttpClient.newHttpClient();

        var request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.pionex.com/api/v1/common/symbols"))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            var symbolsResponse = gson.fromJson(response.body(), SymbolsResponse.class);

            this.supportedSymbols = symbolsResponse.getData().getSymbols().stream().map(SymbolData::getSymbol).collect(Collectors.toSet());

        } catch (IOException | InterruptedException e) {
            log.error("Caught exception receiving symbols list from {}", getExchange(), e);
        }

    }
}
