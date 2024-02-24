package dev.mouradski.ftso.prices.client.gemini;

import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
@ClientEndpoint
@Startup
public class GeminiClientEndpoint extends AbstractClientEndpoint {

    private Set<String> symbols;

    @Override
    protected String getUri() {
        return null;
    }

    @Override
    protected String getExchange() {
        return "gemini";
    }

    @Override
    protected boolean pong(String message) {
        return super.pong(message);
    }

    @Override
    protected void prepareConnection() {
        symbols = new HashSet<>();

        try {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.gemini.com/v1/symbols"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            var geminiSymbols = gson.fromJson(response.body(), String[].class);

            for (var symbol : geminiSymbols) {
                this.symbols.add(symbol.toUpperCase());
            }
        } catch (Exception ignored) {
        }
    }

    @Scheduled(every = "2s")
    public void getTickers() {
        this.lastTickerTime = System.currentTimeMillis();

        if (exchanges.contains(getExchange())) {

            getAssets(true).forEach(base -> {

                getAllQuotesExceptBusd(true).forEach(quote -> {

                    var symbol = base + quote;

                    if (symbols.contains(symbol)) {
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create("https://api.gemini.com/v2/ticker/" + symbol))
                                .header("Content-Type", "application/json")
                                .GET()
                                .build();

                        Uni.createFrom().completionStage(() -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
                                .onItem().transform(response -> gson.fromJson(response.body(), GeminiTicker.class))
                                .subscribe().with(tickerResponse -> {
                                    if (tickerResponse.getSymbol() != null) {
                                        var pair = SymbolHelper.getPair(tickerResponse.getSymbol());
                                        pushTicker(Ticker.builder()
                                                .source(Source.REST)
                                                .exchange(getExchange())
                                                .base(pair.getLeft())
                                                .quote(pair.getRight())
                                                .lastPrice(tickerResponse.getClose())
                                                .timestamp(currentTimestamp())
                                                .build());
                                    }
                                }, failure -> {});
                    }
                });
            });
        }
    }
}
