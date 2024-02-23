package dev.mouradski.ftso.prices.client.cexio;

import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@ApplicationScoped
@ClientEndpoint
@Startup
public class CexioClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return null;
    }

    @Override
    protected String getExchange() {
        return "cexio";
    }

    @Scheduled(every = "2s")
    public void getTickers() {
        this.lastTickerTime = System.currentTimeMillis();
        if ( exchanges.contains(getExchange())) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://cex.io/api/tickers/USD/USDT"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            try {
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());

                var tickerResponse = gson.fromJson(response.body(), TickerResponse.class);

                tickerResponse.getData().forEach(ticker -> {
                    var pair = SymbolHelper.getPair(ticker.getPair());

                    if (getAssets(true).contains(pair.getLeft()) && getAllQuotesExceptBusd(true).contains(pair.getRight())) {
                        pushTicker(Ticker.builder().source(Source.REST).exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(Double.valueOf(ticker.getLast())).timestamp(currentTimestamp()).build());
                    }
                });

            } catch (IOException | InterruptedException e) {
            }
        }
    }
}
