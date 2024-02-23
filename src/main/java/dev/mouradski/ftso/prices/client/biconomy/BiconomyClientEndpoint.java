package dev.mouradski.ftso.prices.client.biconomy;

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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
@ClientEndpoint
@Startup
public class BiconomyClientEndpoint extends AbstractClientEndpoint {
    @Override
    protected String getUri() {
        return "wss://www.biconomy.com/ws";
    }

    @Override
    protected String getExchange() {
        return "biconomy";
    }

    @Override
    protected void subscribeTicker() {
        var pairs = new ArrayList<>();
        getAssets(true).forEach(base -> {
            getAllQuotesExceptBusd(true).forEach(quote -> {
                pairs.add(base + "_" + quote);
            });
        });

        var pairString = pairs.stream().map(v -> "\"" + v + "\"").collect(Collectors.joining(","));
        sendMessage("{ \"method\": \"price.subscribe\", \"params\": [PAIRS], \"id\": ID}"
                .replace("ID", incAndGetIdAsString())
                .replace("PAIRS", pairString));

    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("price.update")) {
            return Optional.empty();
        }

        var priceUpdate = objectMapper.readValue(message, PriceUpdate.class);

        var pair = SymbolHelper.getPair(priceUpdate.getParams()[0]);
        var price = Double.parseDouble(priceUpdate.getParams()[1]);
        var ticker = Ticker.builder().source(Source.WS).base(pair.getLeft()).quote(pair.getRight()).exchange(getExchange()).lastPrice(price).timestamp(currentTimestamp()).build();

        return Optional.of(Collections.singletonList(ticker));
    }

    @Scheduled(every = "160s")
    public void ping() {
        this.sendMessage("{\"method\":\"server.ping\",\"params\":[],\"id\":ID}".replace("ID", incAndGetIdAsString()));
    }
}
