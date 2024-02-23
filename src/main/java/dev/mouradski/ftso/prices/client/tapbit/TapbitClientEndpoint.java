package dev.mouradski.ftso.prices.client.tapbit;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@ClientEndpoint
@Startup
public class TapbitClientEndpoint extends AbstractClientEndpoint {
    @Override
    protected String getUri() {
        return "wss://ws-openapi.tapbit.com/stream/ws";
    }

    @Override
    protected String getExchange() {
        return "tapbit";
    }

    @Override
    protected void subscribeTicker() {
        var pairs = new ArrayList<String>();

        getAssets(true).forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> {
            pairs.add("\"spot/ticker." + base + quote + "\"");
        }));

        sendMessage("{\"op\": \"subscribe\",\"args\": [PAIRS]}".replace("PAIRS", String.join(",", pairs)));
    }

    @Override
    protected boolean pong(String message) {
        if (message.contains("ping")) {
            sendMessage("pong");
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("spot/ticker.")) {
            return Optional.empty();
        }

        var tickerDataWrapper = objectMapper.readValue(message, TickerDataWrapper.class);

        var tickers = new ArrayList<Ticker>();

        tickerDataWrapper.getData().forEach(data -> {
            var pair = SymbolHelper.getPair(data.getSymbol());
            tickers.add(Ticker.builder().source(Source.WS).exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(Double.parseDouble(data.getLastPrice())).timestamp(currentTimestamp()).build());
        });

        return Optional.of(tickers);
    }
}
