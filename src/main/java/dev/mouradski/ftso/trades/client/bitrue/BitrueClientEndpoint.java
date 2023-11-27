package dev.mouradski.ftso.trades.client.bitrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.client.HttpTickers;
import dev.mouradski.ftso.trades.model.Ticker;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
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
public class BitrueClientEndpoint extends AbstractClientEndpoint implements HttpTickers {

    @Override
    protected String getUri() {
        return "wss://ws.bitrue.com/kline-api/ws";
    }

    @Override
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {

        var tradeMessage = gson.fromJson(message, TradeMessage.class);

        if (tradeMessage.getChannel() == null || tradeMessage.getTick() == null
                || tradeMessage.getTick().getData() == null) {
            return Optional.empty();
        }

        var pair = SymbolHelper.getPair(tradeMessage.getChannel().replace("market_", "").replace("_trade_ticker", ""));
        var base = pair.getLeft();
        var quote = pair.getRight();
        var trades = new ArrayList<Trade>();

        tradeMessage.getTick().getData().stream().sorted(Comparator.comparing(BitrueTrade::getTs))
                .forEach(trade -> trades.add(Trade.builder().exchange(getExchange()).price(trade.getPrice())
                        .amount(trade.getVol()).quote(quote).base(base).timestamp(currentTimestamp()).build()));

        return Optional.of(trades);
    }

    @Override
    protected void subscribeTrade() {
        getAssets().stream().forEach(base -> getAllStablecoinQuotesExceptBusd(false).forEach(quote -> this.sendMessage(
                "{\"event\":\"sub\",\"params\":{\"cb_id\":\"CB_ID\",\"channel\":\"market_CB_ID_trade_ticker\"}}"
                        .replaceAll("CB_ID", base + quote))));
    }

    @Override
    protected String getExchange() {
        return "bitrue";
    }

    @Override
    protected long getTimeout() {
        return 30;
    }

    @Override
    protected boolean pong(String message) {

        if (message.contains("ping")) {
            this.sendMessage(message.replaceAll("ping", "pong"));
            return true;
        }

        return false;
    }

    @Override
    public void updateTickers() {
        this.lastTickerTime = System.currentTimeMillis();
        this.lastTickerTime = System.currentTimeMillis();

        if (subscribeTicker && exchanges.contains(getExchange())) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://openapi.bitrue.com/api/v1/ticker/24hr"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            try {

                var response = client.send(request, HttpResponse.BodyHandlers.ofString());

                var tickers = gson.fromJson(response.body(), CryptoItem[].class);


                for (var ticker : tickers) {
                    var pair = SymbolHelper.getPair(ticker.getSymbol());

                    if (getAssets(true).contains(pair.getLeft())
                            && getAllQuotesExceptBusd(true).contains(pair.getRight())) {
                        pushTicker(Ticker.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight())
                                .lastPrice(ticker.getLastPrice()).timestamp(currentTimestamp()).build());
                    }
                }

            } catch (IOException | InterruptedException e) {
            }
        }
    }
}
