package dev.mouradski.ftso.prices.client.digifinex;

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
public class DigifinexClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://openapi.digifinex.com/ws/v1/";
    }

    @Scheduled(every = "2s")
    public void getTickers() {
        this.lastTickerTime = System.currentTimeMillis();
        this.lastTickerTime = System.currentTimeMillis();

        if (exchanges.contains(getExchange())) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://openapi.digifinex.com/v3/ticker"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            try {
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());

                var tickerResponse = gson.fromJson(response.body(), TickerApiResponse.class);

                tickerResponse.getTicker().forEach(ticker -> {
                    var pair = SymbolHelper.getPair(ticker.getSymbol());

                    if (getAssets(true).contains(pair.getLeft()) && getAllQuotesExceptBusd(true).contains(pair.getRight())) {
                        pushTicker(Ticker.builder().source(Source.REST).exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(ticker.getLast()).timestamp(currentTimestamp()).build());
                    }
                });

            } catch (IOException | InterruptedException e) {
            }
        }
    }

    @Override
    protected String getExchange() {
        return "digifinex";
    }

    @Scheduled(every="20s")
    public void ping() {
        this.sendMessage("{\"method\":\"server.ping\", \"param\":[], \"id\":" + incAndGetIdAsString() + "}");
    }
}
