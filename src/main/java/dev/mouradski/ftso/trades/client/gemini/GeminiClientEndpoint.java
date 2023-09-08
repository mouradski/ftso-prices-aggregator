package dev.mouradski.ftso.trades.client.gemini;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Ticker;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@ClientEndpoint
@Startup
public class GeminiClientEndpoint extends AbstractClientEndpoint {

    private HttpClient client = HttpClient.newHttpClient();

    private Set<String> symbols = new HashSet<>();

    @Override
    protected String getUri() {
        return "wss://api.gemini.com/v1/multimarketdata?symbols=" + getMarkets() + "&trades=true&top_of_book=true";
    }

    private String getMarkets() {
        return getAssets(true).stream().map(v -> v + "USDT").collect(Collectors.joining(","));
    }

    @Override
    protected String getExchange() {
        return "gemini";
    }

    @Override
    protected void subscribeTrade() {
    }

    @Override
    protected boolean pong(String message) {
        return super.pong(message);
    }

    @Override
    protected void prepareConnection() {

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
        } catch (Exception e) {
        }
    }

    @Override
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {

        if (!message.contains("trade")) {
            return Optional.empty();
        }

        var evenWrapper = this.objectMapper.readValue(message, GeminiTrade.class);

        var trades = new ArrayList<Trade>();


        evenWrapper.getEvents().stream().sorted(Comparator.comparing(GeminiTrade.Event::getTradeId)).filter(event -> "trade".equals(event.getEventType())).forEach(event -> {
            var symbol = SymbolHelper.getPair(event.getSymbol());
            trades.add(Trade.builder().base(symbol.getLeft()).quote(symbol.getRight()).exchange(getExchange()).timestamp(currentTimestamp()).price(event.getPrice()).amount(event.getAmount()).timestamp(currentTimestamp()).build());
        });

        return Optional.of(trades);
    }

    @Scheduled(every = "5s")
    public void getTickers() {
        this.lastTickerTime = System.currentTimeMillis();
        this.lastTickerTime = System.currentTimeMillis();

        if (subscribeTicker && exchanges.contains(getExchange())) {

            getAssets(true).forEach(base -> {

                getAllQuotesExceptBusd(true).forEach(quote -> {

                    var symbol = base + quote;

                    if (symbols.contains(symbol)) {
                        try {
                            var request = HttpRequest.newBuilder()
                                    .uri(URI.create("https://api.gemini.com/v2/ticker/" + symbol))
                                    .header("Content-Type", "application/json")
                                    .GET()
                                    .build();

                            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

                            var tickerResponse = gson.fromJson(response.body(), GeminiTicker.class);

                            if (tickerResponse.getSymbol() != null) {
                                var pair = SymbolHelper.getPair(tickerResponse.getSymbol());

                                pushTicker(Ticker.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(tickerResponse.getClose()).timestamp(currentTimestamp()).build());
                            }


                        } catch (IOException | InterruptedException e) {
                        }
                    }
                });





            });


        }
    }
}
