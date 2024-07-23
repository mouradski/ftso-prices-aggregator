package dev.mouradski.ftso.prices.client.emirex;

import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class EmirexRestEndpointClient extends AbstractClientEndpoint {

    private Set<String> symbols;

    @Override
    protected String getUri() {
        return null;
    }

    @Override
    protected String getExchange() {
        return "emirex";
    }

    @Override
    protected void prepareConnection() {
        symbols = new HashSet<>();

        try {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.emirex.com/v1/public/symbols"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            var emirexSymbols = gson.fromJson(response.body(), SymbolInfo.class);

            for (var symbol_info : emirexSymbols.getData()) {
                this.symbols.add(symbol_info.getPair());
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Scheduled(every = "3s")
    public void getTickers() {
        this.lastTickerTime = System.currentTimeMillis();

        if (exchanges.contains(getExchange()) && this.isCircuitClosed()) {

            getAssets(true).forEach(base -> {

                getAllQuotes(true).forEach(quote -> {

                    var symbol = base + quote;

                    if (symbols.contains(symbol)) {
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create("https://api.emirex.com/v1/public/ticker?pair=" + symbol))
                                .header("Content-Type", "application/json")
                                .GET()
                                .build();

                        Uni.createFrom().completionStage(() -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
                                .onItem().transform(response -> gson.fromJson(response.body(), TickerResponse.class))
                                .onFailure().invoke(this::catchRestError)
                                .subscribe().with(tickerResponse -> {
                                    if (tickerResponse != null) {
                                        var pair = SymbolHelper.getPair(tickerResponse.getData().getPair());
                                        pushTicker(Ticker.builder()
                                                .source(Source.REST)
                                                .exchange(getExchange())
                                                .base(pair.getLeft())
                                                .quote(pair.getRight())
                                                .lastPrice(tickerResponse.getData().getLast())
                                                .timestamp(currentTimestamp())
                                                .build());
                                    }
                                }, this::catchRestError);
                    }
                });
            });
        }
    }
}

