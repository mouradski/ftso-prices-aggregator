package dev.mouradski.ftso.prices.client.blofin;

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
public class BlofinClientEndpoint extends AbstractClientEndpoint {
    @Override
    protected String getUri() {
        return "wss://openapi.blofin.com/ws/public";
    }

    @Override
    protected String getExchange() {
        return "blofin";
    }

    @Override
    protected void subscribeTicker() {

        getAssets(true).forEach(base -> {
            getAllQuotes(true).forEach(quote -> {
                sendMessage("{\"op\":\"subscribe\",\"args\":[{\"channel\":\"tickers\",\"instId\":\"BASE-QUOTE\"}]}".replace("BASE", base).replace("QUOTE", quote));
            });
        });

    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {

        if (!message.contains("\"channel\":\"tickers\"")) {
            return Optional.empty();
        }

        var marketData = objectMapper.readValue(message, MarketData.class);

        var tickers = new ArrayList<Ticker>();

        marketData.getData().forEach(dataItem -> {
            var pair = SymbolHelper.getPair(dataItem.getInstId());
            tickers.add(Ticker.builder().source(Source.WS).exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).timestamp(currentTimestamp()).lastPrice(Double.valueOf(dataItem.getLast())).build());
        });

        return Optional.of(tickers);
    }
}
