package dev.mouradski.ftso.trades.client.mexc;

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
import java.util.*;


@ApplicationScoped
@ClientEndpoint
@Startup
public class MexcClientEndpoint extends AbstractClientEndpoint implements HttpTickers {

    @Override
    protected String getUri() {
        return "wss://wbs.mexc.com/raw/ws";
    }

    @Override
    protected void subscribeTrade() {
        getAssets(true).forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> this.sendMessage("{\"op\":\"sub.deal\", \"symbol\":\"SYMBOL_QUOTE\"}".replace("SYMBOL", base).replace("QUOTE", quote))));
    }

    @Override
    protected void subscribeTicker() {
        getAssets(true).forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> this.sendMessage("{\"op\":\"sub.ticker\", \"symbol\":\"SYMBOL_QUOTE\"}".replace("SYMBOL", base).replace("QUOTE", quote))));
    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        return super.mapTicker(message);
    }

    @Override
    protected String getExchange() {
        return "mexc";
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
                    .uri(URI.create("https://api.mexc.com/api/v3/ticker/price"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            try {
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());

                var tickerResponse = gson.fromJson(response.body(), PriceTicker[].class);

                Arrays.asList(tickerResponse).forEach(ticker -> {
                    var pair = SymbolHelper.getPair(ticker.getSymbol());

                    if (getAssets(true).contains(pair.getLeft()) && getAllQuotesExceptBusd(true).contains(pair.getRight())) {
                        pushTicker(Ticker.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(ticker.getPrice()).timestamp(currentTimestamp()).build());
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Scheduled(every = "5s")
    public void ping() {
        this.sendMessage("ping");
    }

    @Override
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {
        if (!message.contains("push.deal")) {
            return Optional.empty();
        }

        var tradeData = objectMapper.readValue(message, TradeData.class);

        var pair = SymbolHelper.getPair(tradeData.getSymbol());

        var trades = new ArrayList<Trade>();

        tradeData.getData().getDeals().stream().sorted(Comparator.comparing(Deal::getT)).forEach(deal -> {
            trades.add(Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).price(deal.getP()).amount(deal.getQ()).timestamp(currentTimestamp()).build());
        });

        return Optional.of(trades);
    }
}
