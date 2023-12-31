package dev.mouradski.ftso.trades.client.hotcoin;

import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.client.HttpTickers;
import dev.mouradski.ftso.trades.model.Ticker;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

@ApplicationScoped
@Startup
public class HotcoinTickerUpdater  extends AbstractClientEndpoint implements HttpTickers  {

    @Override
    protected String getUri() {
        return null;
    }

    @Override
    protected String getExchange() {
        return "hotcoin";
    }

    @Override
    public void updateTickers() {
        this.lastTickerTime = System.currentTimeMillis();
        if (subscribeTicker) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.hotcoinfin.com/v1/market/ticker"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            try {
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());

                var tickerResponse = gson.fromJson(response.body(),Tickers.class);

                Arrays.stream(tickerResponse.getTicker()).forEach(data -> {
                    var pair = SymbolHelper.getPair(data.getSymbol());

                    if (getAssets(true).contains(pair.getLeft()) && getAllQuotesExceptBusd(true).contains(pair.getRight())) {
                        var ticker = Ticker.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(data.getLast()).timestamp(currentTimestamp()).build();
                        pushTicker(ticker);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
