package dev.mouradski.ftso.prices.client.crypto;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@ClientEndpoint
@Startup
public class CryptoComClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://stream.crypto.com/v2/market";
    }

    @Override
    protected void subscribeTicker() {
        getAssets().stream().map(String::toUpperCase)
                .forEach(base -> getAllQuotes(true).forEach(quote -> {
                    this.sendMessage("{\"id\": " + incAndGetId()
                            + ",\"method\": \"subscribe\",\"params\": {\"channels\": [\"ticker." + base
                            + "_" + quote + "\"]},\"nonce\": " + new Date().getTime() + "}");
                    this.sendMessage("{\"id\": " + incAndGetId()
                            + ",\"method\": \"subscribe\",\"params\": {\"channels\": [\"ticker." + base
                            + "_" + quote + "\"]},\"nonce\": " + new Date().getTime() + "}");
                }));
    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("ticker.")) {
            return Optional.empty();
        }

        var tickerResponse = objectMapper.readValue(message, dev.mouradski.ftso.prices.client.crypto.Ticker.class);

        var pair = SymbolHelper.getPair(tickerResponse.getResult().getSubscription().replace("ticker.", ""));

        return Optional.of(Collections.singletonList(Ticker.builder().source(Source.WS).exchange(getExchange()).base(pair.getLeft())
                .quote(pair.getRight()).lastPrice(tickerResponse.getResult().getData().get(0).getA())
                .timestamp(currentTimestamp()).build()));
    }

    @Override
    protected String getExchange() {
        return "crypto";
    }

    @Override
    protected boolean pong(String message) {
        if (message.contains("public/heartbeat")) {
            this.sendMessage(message.replace("public/heartbeat", "public/respond-heartbeat"));
            return true;
        }

        return false;
    }
}
