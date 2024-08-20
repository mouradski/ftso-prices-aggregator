package dev.mouradski.ftso.prices.client.bitfinex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@ApplicationScoped
@ClientEndpoint
@Startup
public class BitfinexClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return null;
    }

    @Override
    protected String getExchange() {
        return "bitfinex";
    }

    @Scheduled(every = "1s")
    public void fetchTickers() {
        this.messageReceived();

        if (exchanges.contains(getExchange()) && this.isCircuitClosed()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api-pub.bitfinex.com/v2/tickers?symbols=ALL"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            Uni.createFrom().completionStage(() -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
                    .onItem().transform(response -> {
                        try {
                            return objectMapper.readValue(response.body(), new TypeReference<List<Object[]>>() {});
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .onItem().transformToMulti(tickersResponse -> Multi.createFrom().items(tickersResponse))
                    .subscribe().with(data -> {
                        data.forEach(ticker -> {
                            var pair = SymbolHelper.getPair(ticker[0].toString().replace("t", "").replace("UST", "USDT"));

                            if (getAssets(true).contains(pair.getLeft()) && getAllQuotes(true).contains(pair.getRight())) {

                                pushTicker(Ticker.builder()
                                        .source(Source.REST)
                                        .exchange(getExchange())
                                        .base(pair.getLeft())
                                        .quote(pair.getRight())
                                        .lastPrice(Double.parseDouble(ticker[7].toString()))
                                        .timestamp(currentTimestamp())
                                        .build());


                            }
                        });

                    },this::catchRestError);
        }

    }
}
