package dev.mouradski.ftso.trades.client.bittrex;

import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.client.HttpTickers;
import dev.mouradski.ftso.trades.model.Ticker;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@ApplicationScoped
@ClientEndpoint
@Startup
@Slf4j
public class BittrexClientEndpoint extends AbstractClientEndpoint implements HttpTickers {

    @Override
    protected String getUri() {
        return null;
    }

    @Override
    protected String getExchange() {
        return "bittrex";
    }

    @Override
    public void updateTickers() {
        this.lastTickerTime = System.currentTimeMillis();
        this.lastTradeTime = System.currentTimeMillis();

        if (subscribeTicker && exchanges.contains(getExchange())) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.bittrex.com/v3/markets/tickers"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            try {
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());

                var tickers = gson.fromJson(response.body(), dev.mouradski.ftso.trades.client.bittrex.Ticker[].class);

                for (var ticker : tickers) {
                    var pair = SymbolHelper.getPair(ticker.getSymbol());

                    if (getAssets(true).contains(pair.getLeft()) && getAllQuotesExceptBusd(true).contains(pair.getRight())) {
                        pushTicker(Ticker.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(Double.valueOf(ticker.getLastTradeRate())).timestamp(currentTimestamp()).build());
                    }
                }

            } catch (IOException | InterruptedException e) {
            }
        }
    }
}
