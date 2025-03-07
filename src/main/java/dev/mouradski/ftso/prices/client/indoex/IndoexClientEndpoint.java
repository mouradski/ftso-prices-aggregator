package dev.mouradski.ftso.prices.client.indoex;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@ApplicationScoped
@Startup
public class IndoexClientEndpoint extends AbstractClientEndpoint {

    @Scheduled(every = "1s")
    public void getTickers() {
        if (exchanges.contains(getExchange()) && this.isCircuitClosed()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.indoex.io/getMarketDetails/"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            Uni.createFrom().completionStage(() -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
                    .onItem().transform(response -> {
                        try {
                            return objectMapper.readValue(response.body(), TickerResponse.class);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .onFailure().invoke(this::catchRestError)
                    .onItem().transformToMulti(tickers -> Multi.createFrom().items(tickers.getMarketdetails()))
                    .subscribe().with(tickers -> {
                        tickers.forEach(ticker -> {
                            var pair = SymbolHelper.getPair(ticker.getPair());
                            if (getAssets(true).contains(pair.getLeft()) && getAllQuotes(true).contains(pair.getRight())) {
                                pushTicker(Ticker.builder()
                                        .source(Source.REST)
                                        .exchange(getExchange())
                                        .base(pair.getLeft())
                                        .quote(pair.getRight())
                                        .lastPrice(Double.parseDouble(ticker.getLast()))
                                        .timestamp(currentTimestamp())
                                        .build());
                            }
                        });

                    }, this::catchRestError);
        }
    }

    @Override
    protected String getUri() {
        return null;
    }

    @Override
    protected String getExchange() {
        return "indoex";
    }
}
