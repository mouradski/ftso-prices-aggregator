package dev.mouradski.ftso.trades.client.coinex;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Ticker;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

@ClientEndpoint
@Component
public class CoinexClientEndpoint extends AbstractClientEndpoint {

    private HttpClient client = HttpClient.newHttpClient();

    @Override
    protected String getUri() {
        return "wss://socket.coinex.com";
    }

    @Override
    protected void subscribeTrade() {
        var pairs = new ArrayList<String>();

        getAssets(true).forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> pairs.add("\"" + base + quote + "\"")));

        this.sendMessage("{   \"method\": \"deals.subscribe\",   \"params\": [PAIRS],   \"id\": ID }"
                .replace("PAIRS", String.join(",", pairs))
                .replace("ID", incAndGetIdAsString()));
    }


    @Scheduled(fixedDelay = 30000)
    public void getTickers() {
        if (subscribeTicker && exchanges.contains(getExchange())) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.coinex.com/v1/market/ticker/all"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            try {
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());

                var tickerResponse = gson.fromJson(response.body(), TickerResponse.class);

                tickerResponse.getData().getTicker().entrySet().forEach(tickerEntry -> {
                    var pair = SymbolHelper.getPair(tickerEntry.getKey());

                    if (getAssets(true).contains(pair.getLeft()) && getAllQuotesExceptBusd(true).contains(pair.getRight())) {
                        pushTickers(Ticker.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(tickerEntry.getValue().getLast()).timestamp(currentTimestamp()).build());
                    }
                });

            } catch (IOException | InterruptedException e) {
                //TODO
            }
        }
    }

    @Override
    protected String getExchange() {
        return "coinex";
    }

    @Override
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {
        if (!message.contains("deals.update")) {
            return Optional.empty();
        }

        var dealUpdate = this.objectMapper.readValue(message, DealUpdate.class);

        var pair = SymbolHelper.getPair(dealUpdate.getParams().get(0).toString());

        var trades = new ArrayList<Trade>();

        ((List<Map<String, String>>) dealUpdate.getParams().get(1)).stream()
                .sorted(Comparator.comparing(e -> e.get("time")))
                .forEach(deal -> trades.add(Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight())
                        .price(Double.valueOf(deal.get("price").toString()))
                        .amount(Double.valueOf(deal.get("amount").toString()))
                        .timestamp(currentTimestamp()).build()));

        return Optional.of(trades);
    }

    @Scheduled(fixedDelay = 30000)
    public void ping() {
        this.sendMessage("{\"method\":\"server.ping\",\"params\":[],\"id\": ID}".replace("ID", incAndGetIdAsString()));
    }
}
