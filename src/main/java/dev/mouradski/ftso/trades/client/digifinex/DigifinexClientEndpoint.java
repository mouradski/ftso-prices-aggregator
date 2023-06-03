package dev.mouradski.ftso.trades.client.digifinex;


import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.service.PriceService;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ClientEndpoint
@Component
public class DigifinexClientEndpoint extends AbstractClientEndpoint {

    protected DigifinexClientEndpoint(PriceService priceSender, @Value("${exchanges}") List<String> exchanges, @Value("${assets}") List<String> assets) {
        super(priceSender, exchanges, assets);
    }

    @Override
    protected String getUri() {
        return "wss://openapi.digifinex.com/ws/v1/";
    }


    @Override
    protected void subscribe() {
        Set<String> markets = getAvailableMarkets();

        getAssets().stream().map(String::toUpperCase).forEach(symbol -> {

            if (markets.contains(symbol + "_USD")) {
                this.sendMessage("{\"method\":\"trades.subscribe\", \"params\":[PAIRS], \"id\":ID}".replace("ID", counter.getCount() + "").replace("PAIRS", "\"" + symbol + "_USD\""));
            }

            if (!symbol.equals("USDT") && markets.contains(symbol + "_USDT")) {
                this.sendMessage("{\"method\":\"trades.subscribe\", \"params\":[PAIRS], \"id\":ID}".replace("ID", counter.getCount() + "").replace("PAIRS", "\"" + symbol + "_USDT\""));
            }

            if (!symbol.equals("USDC") && markets.contains(symbol + "_USDC")) {
                this.sendMessage("{\"method\":\"trades.subscribe\", \"params\":[PAIRS], \"id\":ID}".replace("ID", counter.getCount() + "").replace("PAIRS", "\"" + symbol + "_USDC\""));
            }
        });
    }

    @Override
    protected String getExchange() {
        return "digifinex";
    }

    @Scheduled(fixedDelay = 20 * 1000)
    public void ping() {
        this.sendMessage("{\"method\":\"server.ping\", \"param\":[], \"id\":" + counter.getCount() + "}");
    }

    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {

        var trades = new ArrayList<Trade>();

        var tradeResponse = gson.fromJson(message, TradeResponse.class);

        if (tradeResponse.getParams() == null) {
            return trades;
        }

        var tradesArray = gson.toJsonTree(tradeResponse.getParams().get(1)).getAsJsonArray();


        var symbol = SymbolHelper.getSymbol(gson.toJsonTree(tradeResponse.getParams().get(2)).getAsString());


        for (var tradeElement : tradesArray) {
            var trade = gson.fromJson(tradeElement, Trade.class);
            trades.add(Trade.builder().exchange(getExchange()).symbol(symbol.getLeft()).quote(symbol.getRight()).price(trade.getPrice()).amount(trade.getAmount()).build());
        }

        return trades;
    }

    private Set<String> getAvailableMarkets() {
        var client = HttpClient.newHttpClient();

        var request = HttpRequest.newBuilder()
                .uri(URI.create("https://openapi.digifinex.com/v3/markets"))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            var marketData = gson.fromJson(response.body(), MarketData.class);

            return marketData.getData().stream().map(MarketInfo::getMarket).map(String::toUpperCase).collect(Collectors.toSet());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        //TODO log

        return new HashSet<>();
    }

}
