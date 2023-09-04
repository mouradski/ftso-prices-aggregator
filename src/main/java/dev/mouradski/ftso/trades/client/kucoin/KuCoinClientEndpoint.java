package dev.mouradski.ftso.trades.client.kucoin;

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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@ClientEndpoint
@Startup
public class KuCoinClientEndpoint extends AbstractClientEndpoint {

    private String token;
    private String instance;

    @Override
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {
        var kucoinTrade = this.objectMapper.readValue(message, KucoinTrade.class);

        if (kucoinTrade.getData() == null) {
            return Optional.empty();
        }

        return Optional.of(Collections.singletonList(Trade.builder()
                .exchange(getExchange())
                .price(kucoinTrade.getData().getPrice())
                .amount(kucoinTrade.getData().getSize())
                .base(kucoinTrade.getData().getSymbol().split("-")[0])
                .quote(kucoinTrade.getData().getSymbol().split("-")[1])
                .timestamp(currentTimestamp())
                .build()));
    }



    @Override
    protected String getUri() {
        return instance + "?token=" + token;
    }

    @Override
    protected void prepareConnection() {
        var client = HttpClient.newHttpClient();

        var request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.kucoin.com/api/v1/bullet-public"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();

        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            var kuCoinTokenResponse = gson.fromJson(response.body(), dev.mouradski.ftso.trades.client.kucoin.KuCoinTokenResponse.class);

            this.token = kuCoinTokenResponse.getData().getToken();
            this.instance = kuCoinTokenResponse.getData().getInstanceServers().get(0).getEndpoint();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void subscribeTrade() {
        getAssets(true).forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> {
            subscribeToTrades(base.toUpperCase() + "-" + quote);
        }));
    }

    @Override
    protected void subscribeTicker() {
        getAssets(true).forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> {
            subscribeToTicker(base.toUpperCase() + "-" + quote);
        }));
    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("/market/ticker")) {
            return Optional.empty();
        }
        var tickerMessage = objectMapper.readValue(message, TickerMessage.class);

        var pair = SymbolHelper.getPair(tickerMessage.getTopic().split(":")[1]);

        return Optional.of(Collections.singletonList(Ticker.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(tickerMessage.getData().getPrice()).timestamp(currentTimestamp()).build()));
    }

    @Override
    protected String getExchange() {
        return "kucoin";
    }


    private void subscribeToTrades(String symbol) {
        var subscribeMessage = "{\"type\":\"subscribe\",\"topic\":\"/market/match:" + symbol + "\",\"privateChannel\":false,\"response\":true}";
        this.sendMessage(subscribeMessage);
    }

    private void subscribeToTicker(String symbol) {
        var subscribeMessage = "{\"type\":\"subscribe\",\"topic\":\"/market/ticker:" + symbol + "\",\"privateChannel\":false,\"response\":true}";
        this.sendMessage(subscribeMessage);
    }

    @Scheduled(every="50s")
    public void ping() {
        this.sendMessage("{\"type\":\"ping\", \"id\":" + new Date().getTime() + "}");
    }
}
