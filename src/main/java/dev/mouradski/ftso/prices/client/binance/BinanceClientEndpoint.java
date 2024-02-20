package dev.mouradski.ftso.prices.client.binance;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@ClientEndpoint
@Startup
public class BinanceClientEndpoint extends AbstractClientEndpoint {

    private final String websocketApiBase = "wss://stream.binance.com:9443/stream?streams=";

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("ticker")) {
            return Optional.empty();
        }

        var binanceTicker = objectMapper.readValue(message, BinanceEvent.class);

        Pair<String, String> pair = SymbolHelper.getPair(binanceTicker.getData().getS());

        return Optional.of(Collections.singletonList(Ticker.builder().timestamp(currentTimestamp()).exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight())
                .lastPrice(binanceTicker.getData().getC()).build()));
    }

    @Override
    protected String getUri() {
        return getWebsocketApiBase() + getPairs();
    }

    private String getPairs() {

        var pairs = getAssets().stream()
                .flatMap(asset -> getAllQuotes(false).stream()
                        .filter(quote -> !quote.equals(asset))
                        .map(quote -> asset + quote + "@ticker")).toList();

        return String.join("/", pairs);
    }


    @Override
    protected String getExchange() {
        return "binance";
    }

    protected String getWebsocketApiBase() {
        return websocketApiBase;
    }
}
