package dev.mouradski.ftso.prices.client.bingx;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@ClientEndpoint
@Startup
public class BingxClientEndpoint extends AbstractClientEndpoint {
    @Override
    protected String getUri() {
        return "wss://open-api-ws.bingx.com/market";
    }

    @Override
    protected void subscribe() {
        getAssets(true).forEach(base -> {
            getAllQuotesExceptBusd(true).forEach(quote -> {
                this.sendMessage("{ \"id\": \"ID\", \"reqType\": \"sub\", \"dataType\": \"BASE-QUOTE@lastPrice\" }".replace("ID", incAndGetIdAsString()).replace("BASE", base).replace("QUOTE", quote));
            });
        });
    }


    @Override
    protected String getExchange() {
        return "bingx";
    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("@lastPrice")) {
            return Optional.empty();
        }

        var lastPriceResponse = this.objectMapper.readValue(message, TickerResponse.class);

        var pair = SymbolHelper.getPair(lastPriceResponse.getData().getSymbol());

        return Optional.of(Collections.singletonList(Ticker.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight())
                .lastPrice(Double.valueOf(lastPriceResponse.getData().getLastPrice())).timestamp(currentTimestamp()).build()));
    }

    @Override
    protected boolean pong(String message) {
        if (message.contains("Ping")) {
            this.sendMessage("Pong");
            return true;
        }

        return false;
    }
}
