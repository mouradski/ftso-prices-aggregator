package dev.mouradski.ftso.trades.client.btse;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ApplicationScoped
@ClientEndpoint
@Startup
public class BtseClientEndpoint extends AbstractClientEndpoint implements HttpTickers {

    @Override
    protected String getUri() {
        return "wss://ws.btse.com/ws/spot";
    }

    @Override
    protected void subscribeTrade() {
        var pairs = new ArrayList<String>();
        getAssets(true).stream()
                .filter(base -> !getAllQuotes(true).contains(base))
                .forEach(symbol -> getAllQuotesExceptBusd(true).forEach(quote -> {
                    var symbolId = symbol + "-" + quote;
                    pairs.add("\"tradeHistoryApi:SYMBOL\"".replace("SYMBOL", symbolId));
                    this.sendMessage("{\"op\": \"subscribe\",\"args\": [\"tradeHistoryApi:SYMBOL\"]}".replace("SYMBOL",
                            symbolId));
                }));
    }


    @Override
    protected String getExchange() {
        return "btse";
    }

    @Override
    protected boolean pong(String message) {
        if (message.contains("ping")) {
            this.sendMessage("pong");
            return true;
        }

        return false;
    }

    @Override
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {

        if (!message.contains("tradeId")) {
            return Optional.empty();
        }

        var tradeHistoryResponse = this.objectMapper.readValue(message, TradeHistoryResponse.class);

        var trades = new ArrayList<Trade>();

        tradeHistoryResponse.getData().forEach(tradeHistoryData -> {
            var pair = SymbolHelper.getPair(tradeHistoryData.getSymbol());

            trades.add(Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight())
                    .price(tradeHistoryData.getPrice()).amount(tradeHistoryData.getSize())
                    .timestamp(currentTimestamp()).build());
        });

        return Optional.of(trades);
    }

    @Override
    public void updateTickers() {
        this.lastTickerTime = System.currentTimeMillis();
        this.lastTickerTime = System.currentTimeMillis();

        if (subscribeTicker && exchanges.contains(getExchange())) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.btse.com/spot/api/v3.2/market_summary"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            try {
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());

                var tickerResponse = gson.fromJson(response.body(), TickerData[].class);

                Stream.of(tickerResponse).forEach(e -> {
                    var pair = SymbolHelper.getPair(e.getSymbol());

                    if (getAssets(true).contains(pair.getLeft()) && getAllQuotesExceptBusd(true).contains(pair.getRight())) {
                        pushTicker(Ticker.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(e.getLast()).timestamp(currentTimestamp()).build());
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
