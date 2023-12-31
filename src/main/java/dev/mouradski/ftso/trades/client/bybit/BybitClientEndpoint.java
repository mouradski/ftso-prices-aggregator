package dev.mouradski.ftso.trades.client.bybit;

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
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@ClientEndpoint
@Startup
public class BybitClientEndpoint extends AbstractClientEndpoint implements HttpTickers {

    @Override
    protected String getUri() {
        return "wss://stream.bybit.com/spot/quote/ws/v2";
    }

    @Override
    protected void subscribeTrade() {
        getAssets().stream().map(String::toUpperCase)
                .forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> this.sendMessage(
                        "{\"topic\":\"trade\", \"params\":{\"symbol\":\"SYMBOLQUOTE\", \"binary\":false}, \"event\":\"sub\"}"
                                .replace("SYMBOL", base).replace("QUOTE", quote))));
    }

    @Override
    protected boolean httpTicker() {
        return true;
    }

    @Override
    public void updateTickers() {
        this.lastTickerTime = System.currentTimeMillis();
        if (subscribeTicker && exchanges.contains(getExchange())) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.bybit.com/v5/market/tickers?category=spot"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            try {
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());

                var tickerResponse = gson.fromJson(response.body(), TickerResponse.class);

                tickerResponse.getResult().getList().forEach(ticker -> {
                    var pair = SymbolHelper.getPair(ticker.getSymbol());

                    if (getAssets(true).contains(pair.getLeft()) && getAllQuotesExceptBusd(true).contains(pair.getRight())) {
                        pushTicker(Ticker.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(ticker.getLastPrice()).timestamp(currentTimestamp()).build());
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        return super.mapTicker(message);
    }

    @Override
    protected String getExchange() {
        return "bybit";
    }

    @Scheduled(every="30s")
    public void ping() {
        this.sendMessage("{\"ping\":" + new Date().getTime() + "}");
    }

    @Override
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {

        if (!message.contains("trade") || !message.contains("data")) {
            return Optional.empty();
        }

        var bybitTrade = objectMapper.readValue(message, BybitTrade.class);

        Pair<String, String> pair = SymbolHelper.getPair(bybitTrade.getParams().getSymbol());

        return Optional.of(Collections.singletonList(Trade.builder().timestamp(currentTimestamp()).exchange(getExchange())
                .base(pair.getLeft()).quote(pair.getRight())
                .price(Double.parseDouble(bybitTrade.getData().getP()))
                .amount(Double.parseDouble(bybitTrade.getData().getQ())).build()));
    }
}
