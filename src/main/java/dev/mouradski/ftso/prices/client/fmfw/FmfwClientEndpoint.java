package dev.mouradski.ftso.prices.client.fmfw;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
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
public class FmfwClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://api.fmfw.io/api/3/ws/public";
    }

    @Override
    protected void subscribeTicker() {

        var pairs = new ArrayList<String>();

        getAssets().stream().map(String::toUpperCase).forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> pairs.add("\"" + base + quote + "\"")));

        this.sendMessage("{\"method\":\"subscribe\", \"ch\":\"ticker/price/1s\", \"params\":{\"symbols\": [PAIRS]}, \"id\": ID}".replace("ID", incAndGetIdAsString()).replace("PAIRS", String.join(",", pairs)));
    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("ticker/price/1s")) {
            return Optional.empty();
        }

        var tickerResponse = objectMapper.readValue(message, TickerResponse.class);

        var tickers = new ArrayList<Ticker>();

        tickerResponse.getData().entrySet().forEach(data -> {
            var pair = SymbolHelper.getPair(data.getKey());
            tickers.add(Ticker.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(data.getValue().getClosePrice()).timestamp(currentTimestamp()).build());
        });

        return Optional.of(tickers);
    }

    @Override
    protected String getExchange() {
        return "fmfw";
    }
}
