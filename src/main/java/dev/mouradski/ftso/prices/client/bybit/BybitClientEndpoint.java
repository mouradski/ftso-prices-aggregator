package dev.mouradski.ftso.prices.client.bybit;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
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
import java.util.Date;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@ClientEndpoint
@Startup
public class BybitClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://stream.bybit.com/spot/quote/ws/v2";
    }

    @Override
    protected boolean httpTicker() {
        return true;
    }

    @Scheduled(every = "2s")
    public void getTickers() {
        this.lastTickerTime = System.currentTimeMillis();
        if (exchanges.contains(getExchange())) {
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

            } catch (IOException | InterruptedException ignored) {
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
}
