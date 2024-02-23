package dev.mouradski.ftso.prices.client.whitebit;

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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
@ClientEndpoint
@Slf4j
@Startup
public class WhitebitClientEndpoint extends AbstractClientEndpoint {

    private Set<String> supportedSymbols;

    @Override
    protected String getUri() {
        return "wss://api.whitebit.com/ws";
    }

    @Override
    protected void subscribeTicker() {
        var pairs = new ArrayList<String>();

        getAssets(true).forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> {
            var pair = base + "_" + quote;

            if (supportedSymbols.contains(pair)) {
                pairs.add("\"" + pair + "\"");
            }
        }));

        this.sendMessage("{\"id\": ID,\"method\": \"lastprice_subscribe\",\"params\": [PAIRS]}"
                .replace("ID", incAndGetIdAsString())
                .replace("PAIRS", String.join(",", pairs)));

    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("lastprice_update")) {
            return Optional.empty();
        }

        var priceUpdate = objectMapper.readValue(message, PriceUpdate.class);

        var pair = SymbolHelper.getPair(priceUpdate.getSymbol());

        return Optional.of(Collections.singletonList(Ticker.builder().source(Source.WS).exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(priceUpdate.getLastPrice()).timestamp(currentTimestamp()).build()));
    }

    @Override
    protected String getExchange() {
        return "whitebit";
    }

    @Override
    protected void prepareConnection() {
        var client = HttpClient.newHttpClient();

        var request = HttpRequest.newBuilder()
                .uri(URI.create("https://whitebit.com/api/v4/public/markets"))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            var markets = gson.fromJson(response.body(), MarketPair[].class);

            this.supportedSymbols = Stream.of(markets).map(MarketPair::getName).collect(Collectors.toSet());

        } catch (IOException | InterruptedException e) {
            log.error("Caught exception collecting markets from {}", getExchange());
        }
    }

    @Scheduled(every="30s")
    public void ping() {
        this.sendMessage("{\"id\": ID,\"method\": \"ping\",\"params\": []}".replace("ID", incAndGetIdAsString()));
    }
}
