package dev.mouradski.ftso.trades.client.p2b;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@ClientEndpoint
@Startup
public class P2BClientEndpoint extends AbstractClientEndpoint {

    private HttpClient client = HttpClient.newHttpClient();

    @Override
    protected String getUri() {
        return "wss://apiws.p2pb2b.com";
    }

    @Override
    protected void subscribeTrade() {
        var subscribeTemplate = "{\"method\":\"deals.subscribe\",\"params\":[SYMBOLS],\"id\":ID}";
        
        var symbols = new ArrayList<String>();

        getAssets(true).forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> symbols.add("\"" + base + "_" + quote + "\"")));

        this.sendMessage(subscribeTemplate.replace("SYMBOLS", String.join(",", symbols)).replace("ID", incAndGetIdAsString()));
    }


    @Scheduled(every = "3s")
    public void getTickers() {
        this.lastTickerTime = System.currentTimeMillis();

        if (subscribeTicker && exchanges.contains(getExchange())) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.p2pb2b.com/api/v2/public/tickers"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            try {
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());

                var tickerResponse = gson.fromJson(response.body(), TickerApiResponse.class);

                tickerResponse.getResult().forEach((key, value) -> {
                    var pair = SymbolHelper.getPair(key);

                    if (getAssets(true).contains(pair.getLeft()) && getAllQuotesExceptBusd(true).contains(pair.getRight())) {
                        pushTicker(Ticker.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(value.getTicker().getLast()).timestamp(currentTimestamp()).build());
                    }
                });

            } catch (IOException | InterruptedException e) {
            }
        }
    }

    @Override
    protected String getExchange() {
        return "p2b";
    }

    @Override
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {
        if (!message.contains("deals.update")) {
            return Optional.empty();

        }

        var trades = new ArrayList<Trade>();

        var dealsResponse = objectMapper.readValue(message, DealsResponse.class);

        var pair = SymbolHelper.getPair(dealsResponse.getParams().get(0).toString());

        var deals = objectMapper.convertValue(dealsResponse.getParams().get(1), new TypeReference<List<Deal>>(){});

        deals.stream().sorted(Comparator.comparing(Deal::getTime)).forEach(deal -> trades.add(Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight())
                .price(Double.valueOf(deal.getPrice())).amount(Double.valueOf(deal.getAmount())).timestamp(currentTimestamp()).build()));

        return Optional.of(trades);
    }

    @Scheduled(every = "30s")
    public void ping() {
        this.sendMessage("{\"method\":\"server.ping\",\"params\":[],\"id\":ID}".replace("ID", incAndGetIdAsString()));
    }
}
