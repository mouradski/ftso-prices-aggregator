package dev.mouradski.ftso.prices.client.gateio;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
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
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("spot.tickers") || message.contains("status")) {
            return Optional.empty();
        }

        var gateIoTicker = objectMapper.readValue(message, GeteIOTicker.class);

        var pair = SymbolHelper.getPair(gateIoTicker.getResult().getCurrencyPair());

        return Optional.of(Collections
                .singletonList(Ticker.builder().source(Source.WS).exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight())
                        .lastPrice(gateIoTicker.getResult().getLast()).timestamp(currentTimestamp()).build()));
    }

    @Override
    protected String getUri() {
        return "wss://api.gateio.ws/ws/v4/";
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
