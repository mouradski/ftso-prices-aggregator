package dev.mouradski.ftso.prices.client.kraken;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class KrakenFutureClientEndpoint extends KrakenClientEndpoint {

    private Set<String> symbols = new HashSet<>();
    @Override
    protected String getExchange() {
        return "krakenfuture";
    }

    @Override
    protected String getUri() {
        return "wss://futures.kraken.com/ws";
    }

    @Override
    protected void subscribeTicker() {

        var subTemplateMsg = """
                {
                        "event": "subscribe",
                        "feed": "ticker",
                        "product_ids": [PAIRS]
                    }
                """;

        this.sendMessage(subTemplateMsg.replace("PAIRS", symbols.stream().map(v -> "\"" + v + "\"").collect(Collectors.joining(","))));
    }

    @Override
    protected void prepareConnection() {
        var client = HttpClient.newHttpClient();

        var request = HttpRequest.newBuilder()
                .uri(URI.create("https://futures.kraken.com/derivatives/api/v3/tickers"))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            var markets = objectMapper.readValue(response.body(), TickerResponse.class);

            markets.getTickers().stream().filter(v -> v.getSymbol().startsWith("PF_")).forEach(ticker -> {

                var pair = SymbolHelper.getPair(ticker.getSymbol().replace("PF_", ""));

                if (getAssets(true).contains(pair.getLeft()) && getAllQuotes(true).contains(pair.getRight())) {
                    this.symbols.add(ticker.getSymbol());
                    this.pushTicker(Ticker.builder().exchange(getExchange()).timestamp(currentTimestamp()).source(Source.REST).lastPrice(ticker.getLast()).base(pair.getLeft()).quote(pair.getRight()).build());
                }
            });

        } catch (IOException | InterruptedException e) {
            log.error("Caught exception collecting markets from {}", getExchange());
        }
    }


    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("last")) {
            return Optional.empty();
        }

        var tickerData = objectMapper.readValue(message, TickerData.class);

        var pair = SymbolHelper.getPair(tickerData.getProductId().replace("PF_", ""));


        return Optional.of(Collections.singletonList(Ticker.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).source(Source.WS).lastPrice(tickerData.getLast()).timestamp(currentTimestamp()).build()));
    }


}
