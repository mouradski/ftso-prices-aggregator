package dev.mouradski.ftso.prices.client.luno;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.client.bitvavo.TickerData;
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
public class LunoClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return null;
    }

    @Override
    protected String getExchange() {
        return "luno";
    }

    @Scheduled(every = "2s")
    public void getTickers() {
        this.messageReceived();
        if (exchanges.contains(getExchange()) && this.isCircuitClosed()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.luno.com/api/1/tickers"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            Uni.createFrom().completionStage(() -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
                    .onItem().transform(response -> {
                        try {
                            return objectMapper.readValue(response.body(), Tickers.class);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .onItem().transformToMulti(tickerResponse -> Multi.createFrom().iterable(tickerResponse.getTickers()))
                    .onFailure().invoke(this::catchRestError)
                    .subscribe().with(tickerEntry -> {
                        if (tickerEntry.getPair() != null && tickerEntry.getLastTrade() != null) {
                            var pair = SymbolHelper.getPair(tickerEntry.getPair());
                            if (getAssets(true).contains(pair.getLeft()) && getAllQuotes(true).contains(pair.getRight())) {
                                pushTicker(Ticker.builder()
                                        .source(Source.REST)
                                        .exchange(getExchange())
                                        .base(pair.getLeft())
                                        .quote(pair.getRight())
                                        .lastPrice(Double.parseDouble(tickerEntry.getLastTrade()))
                                        .timestamp(currentTimestamp())
                                        .build());
                            }
                        }
                    }, this::catchRestError);
        }
    }
}
