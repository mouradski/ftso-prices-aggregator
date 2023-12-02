package dev.mouradski.ftso.trades.client.latoken;

import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.client.HttpTickers;
import dev.mouradski.ftso.trades.client.bybit.TickerResponse;
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
public class LatokenTickerUpdater extends AbstractClientEndpoint implements HttpTickers {

    @Override
    public void updateTickers() {
        this.lastTickerTime = System.currentTimeMillis();
        if (subscribeTicker && exchanges.contains(getExchange())) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.latoken.com/v2/ticker"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            try {
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());

                var tickerResponse = gson.fromJson(response.body(), Data[].class);

                Arrays.stream(tickerResponse).forEach(data -> {
                    var pair = SymbolHelper.getPair(data.getSymbol());

                    if (getAssets(true).contains(pair.getLeft()) && getAllQuotesExceptBusd(true).contains(pair.getRight())) {
                        var ticker = Ticker.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(data.getLastPrice()).timestamp(currentTimestamp()).build();
                        pushTicker(ticker);
                    }
                });

            } catch (IOException | InterruptedException e) {
                //TODO
            }
        }
    }

    @Override
    protected String getUri() {
        return null;
    }

    @Override
    protected String getExchange() {
        return "latoken";
    }
}
