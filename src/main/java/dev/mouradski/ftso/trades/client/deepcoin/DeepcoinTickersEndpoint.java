package dev.mouradski.ftso.trades.client.deepcoin;

import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Ticker;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

@ApplicationScoped
public class DeepcoinTickersEndpoint extends AbstractClientEndpoint {

    @Scheduled(every = "3s")
    public void getTickers() {

        if (!exchanges.contains(getExchange())) {
            return;
        }

        this.lastTickerTime = System.currentTimeMillis();

        Arrays.asList("https://api.deepcoin.com/deepcoin/market/tickers?instType=SPOT", "https://api.deepcoin.com/deepcoin/market/tickers?instType=SWAP").forEach(url -> {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            try {
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());

                var tickerResponse = gson.fromJson(response.body(), TickersResponse.class);

                tickerResponse.getData().forEach(ticker -> {

                    var pair = SymbolHelper.getPair(ticker.getInstId().replace("-SWAP", ""));

                    if (getAssets(true).contains(pair.getLeft()) && getAllQuotesExceptBusd(true).contains(pair.getRight())) {
                        pushTicker(Ticker.builder().exchange("SWAP".equals(ticker.getInstType()) ? (getExchange() + "swap") : getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(Double.valueOf(ticker.getLast())).timestamp(currentTimestamp()).build());
                    }
                });

            } catch (IOException | InterruptedException e) {
            }
        });


    }

    @Override
    protected String getUri() {
        return null;
    }

    @Override
    protected String getExchange() {
        return "deepcoin";
    }
}
