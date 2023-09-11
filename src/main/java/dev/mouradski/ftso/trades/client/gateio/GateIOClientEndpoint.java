package dev.mouradski.ftso.trades.client.gateio;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonParser;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Ticker;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@ClientEndpoint
@Startup
@Slf4j
public class GateIOClientEndpoint extends AbstractClientEndpoint {

    private List<String> supportedSymbols = new ArrayList<>();

    @Override
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {

        if (!message.contains("spot.trades") || message.contains("status")) {
            return Optional.empty();
        }

        var jelement = JsonParser.parseString(message);
        var jobject = jelement.getAsJsonObject();
        var result = jobject.getAsJsonObject("result");

        var gateIOTrade = gson.fromJson(result, GateIOTrade.class);

        return Optional.of(Collections
                .singletonList(Trade.builder().exchange(getExchange()).base(gateIOTrade.getCurrencyPair().split("_")[0])
                        .quote(gateIOTrade.getCurrencyPair().split("_")[1]).price(gateIOTrade.getPrice())
                        .amount(gateIOTrade.getAmount()).timestamp(currentTimestamp()).build()));

    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("spot.tickers") || message.contains("status")) {
            return Optional.empty();
        }

        var gateIoTicker = objectMapper.readValue(message, GeteIOTicker.class);

        var pair = SymbolHelper.getPair(gateIoTicker.getResult().getCurrencyPair());

        return Optional.of(Collections
                .singletonList(Ticker.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight())
                        .lastPrice(gateIoTicker.getResult().getLast()).timestamp(currentTimestamp()).build()));
    }

    @Override
    protected String getUri() {
        return "wss://api.gateio.ws/ws/v4/";
    }

    @Override
    protected void subscribeTrade() {

        var pairs = new ArrayList<String>();

        getAssets().stream().map(String::toUpperCase).filter(v -> !"DGB".equals(v))
                .forEach(base -> getAllStablecoinQuotesExceptBusd(true)
                        .forEach(quote -> {
                            var pair = base + "_" + quote;
                            if (this.supportedSymbols.contains(pair))
                                pairs.add("\"" + pair + "\"");
                        }));
        var timestamp = System.currentTimeMillis();
        var subscribeMessage = String.format(
                "{\"time\": %d, \"channel\": \"spot.trades\", \"event\": \"subscribe\", \"payload\": [%s]}", timestamp,
                String.join(",", pairs));

        this.sendMessage(subscribeMessage);
    }

    @Override
    protected void subscribeTicker() {

        var pairs = new ArrayList<String>();

        getAssets().stream().map(String::toUpperCase).filter(v -> !"DGB".equals(v))
                .forEach(base -> getAllStablecoinQuotesExceptBusd(true)
                        .forEach(quote -> {
                            var pair = base + "_" + quote;
                            if (this.supportedSymbols.contains(pair))
                                pairs.add("\"" + pair + "\"");
                        }));

        var timestamp = System.currentTimeMillis();
        var subscribeMessage = String.format(
                "{\"time\": %d, \"channel\": \"spot.tickers\", \"event\": \"subscribe\", \"payload\": [%s]}", timestamp,
                String.join(",", pairs));

        this.sendMessage(subscribeMessage);
    }

    @Override
    protected String getExchange() {
        return "gateio";
    }

    @Scheduled(every = "30s")
    public void ping() {
        this.sendMessage("{\"method\":\"server.ping\"}");
    }

    @Override
    protected void prepareConnection() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.gateio.ws/api/v4/spot/currency_pairs"))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            SymbolData[] symbolResponse = objectMapper
                    .readValue(response.body(), SymbolData[].class);

            var symbols = new ArrayList<String>();

            for (var symbol : symbolResponse) {
                symbols.add(symbol.getId());
            }
            this.supportedSymbols = symbols;
        } catch (IOException | InterruptedException e) {
            log.error("Caught exception collecting markets from {}: {}", getExchange(), e.getMessage());
        }
    }
}
