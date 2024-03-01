package dev.mouradski.ftso.prices.client.delta;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@ClientEndpoint
@Startup
public class DeltaClientEndpoint extends AbstractClientEndpoint {
    @Override
    protected String getUri() {
        return "wss://socket.delta.exchange";
    }

    @Override
    protected String getExchange() {
        return "delta";
    }

    @Override
    protected void subscribeTicker() {

        getAssets(true).forEach(base -> {
            getAllQuotesExceptBusd(true).forEach(quote -> {
                sendMessage("{\"type\": \"subscribe\",\"payload\": {\"channels\": [{\"name\": \"v2/ticker\",\"symbols\": [\"BASE_QUOTE\"]} ]}}".replace("BASE", base).replace("QUOTE", quote));
            });
        });
    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("v2/ticker")) {
            return Optional.empty();
        }

        var tickerResponse = objectMapper.readValue(message, TickerResponse.class);

        var pair = SymbolHelper.getPair(tickerResponse.getSymbol());
        var ticker = Ticker.builder().source(Source.WS).exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(tickerResponse.getClose()).timestamp(currentTimestamp()).build();

        return Optional.of(Collections.singletonList(ticker));
    }

    @Scheduled(every = "29s")
    public void ping() {
        this.sendMessage("{\"type\": \"ping\"}");
    }
}
