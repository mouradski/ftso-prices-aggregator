package dev.mouradski.ftso.prices.client.poloniex;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
@ClientEndpoint
@Startup
public class PoloniexClientEndpoint extends AbstractClientEndpoint {
    @Override
    protected String getUri() {
        return "wss://ws.poloniex.com/ws/public";
    }

    @Override
    protected String getExchange() {
        return "poloniex";
    }



    @Override
    protected void subscribeTicker() {
        var pairs = new ArrayList<>();
        getAssets(true).forEach(base -> {
            getAllQuotes(true).forEach(quote -> {
                pairs.add(base + "_" + quote);
            });
        });

        this.sendMessage("{   \"event\": \"subscribe\",   \"channel\": [\"ticker\"],   \"symbols\": [SYMBOLS] }".replace("SYMBOLS", pairs.stream().map(v -> "\"" + v + "\"").collect(Collectors.joining(","))));
    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("ticker")) {
            return Optional.empty();
        }

        var tickerMessage = objectMapper.readValue(message, TickerMessage.class);

        var tickers = new ArrayList<Ticker>();

        tickerMessage.getData().forEach(data -> {
            var pair = SymbolHelper.getPair(data.getSymbol());
            tickers.add(Ticker.builder().source(Source.WS).exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(Double.parseDouble(data.getClose())).timestamp(currentTimestamp()).build());
        });

        return Optional.of(tickers);
    }

    @Scheduled(every = "30s")
    public void ping() {
        this.sendMessage("{\"event\": \"ping\"}");
    }
}
