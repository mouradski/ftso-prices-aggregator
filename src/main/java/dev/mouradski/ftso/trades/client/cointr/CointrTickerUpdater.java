package dev.mouradski.ftso.trades.client.cointr;

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
public class CointrTickerUpdater extends AbstractClientEndpoint implements HttpTickers {
    @Override
    protected String getUri() {
        return null;
    }

    @Override
    protected String getExchange() {
        return "cointr";
    }

    @Override
    public void updateTickers() {
        this.lastTickerTime = System.currentTimeMillis();

        if (exchanges.contains(getExchange())) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.cointr.pro/v1/spot/market/tickers"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            try {
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());

                var tickerResponse = gson.fromJson(response.body(), TickersResponse.class);

                Arrays.stream(tickerResponse.getData()).forEach(data -> {
                    var pair = SymbolHelper.getPair(data.getInstId());

                    if (getAssets(true).contains(pair.getLeft()) && getAllQuotesExceptBusd(true).contains(pair.getRight())) {
                        var ticker = Ticker.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(Double.parseDouble(data.getLastPx())).timestamp(currentTimestamp()).build();
                        pushTicker(ticker);
                    }
                });

            } catch (IOException | InterruptedException ignored) {
            }
        }
    }
}
