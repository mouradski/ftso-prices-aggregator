package dev.mouradski.ftso.trades.client.bitstamp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonParser;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.client.HttpTickers;
import dev.mouradski.ftso.trades.model.Ticker;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@ClientEndpoint
@Startup
public class BitstampClientEndpoint extends AbstractClientEndpoint implements HttpTickers {

    @Override
    protected String getUri() {
        return "wss://ws.bitstamp.net";
    }

    @Override
    protected void subscribeTrade() {
        getAssets().forEach(base -> getAllQuotesExceptBusd(false).forEach(quote -> this.sendMessage(
                "{\"event\": \"bts:subscribe\", \"data\": {\"channel\": \"live_trades_" + base + quote + "\"}}")));
    }

    @Override
    protected String getExchange() {
        return "bitstamp";
    }

    @Override
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {
        if (message.contains("\"event\":\"trade\"")) {
            var jsonObject = JsonParser.parseString(message).getAsJsonObject();

            var channelName = jsonObject.get("channel").getAsString();
            var symbolId = channelName.substring(12).toUpperCase(); // remove "live_trades_" and convert to upper case

            var tradeData = jsonObject.get("data").getAsJsonObject();

            var trade = new Trade();
            trade.setPrice(tradeData.get("price").getAsDouble());
            trade.setAmount(tradeData.get("amount").getAsDouble());
            trade.setTimestamp(tradeData.get("timestamp").getAsLong() * 1000); // timestamp is sent in seconds
            trade.setExchange(getExchange());

            var pair = SymbolHelper.getPair(symbolId);

            trade.setBase(pair.getLeft());
            trade.setQuote(pair.getRight());

            return Optional.of(List.of(trade));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void updateTickers() {
        this.lastTickerTime = System.currentTimeMillis();
        this.lastTickerTime = System.currentTimeMillis();

        if (subscribeTicker && exchanges.contains(getExchange())) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.bitstamp.net/api/v2/ticker/"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            try {
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());

                var tickers = gson.fromJson(response.body(), dev.mouradski.ftso.trades.client.bitstamp.Ticker[].class);

                for (var ticker : tickers) {
                    var pair = SymbolHelper.getPair(ticker.getPair());
                    if (getAssets(true).contains(pair.getLeft()) && getAllQuotesExceptBusd(true).contains(pair.getRight())) {
                        pushTicker(Ticker.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(ticker.getLast()).timestamp(currentTimestamp()).build());
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
