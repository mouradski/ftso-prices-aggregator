package dev.mouradski.ftso.prices.client.exmo;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
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
public class ExmoClientEndpoint extends AbstractClientEndpoint {
    @Override
    protected String getUri() {
        return "wss://ws-api.exmo.com:443/v1/public";
    }

    @Override
    protected String getExchange() {
        return "exmo";
    }

    @Override
    protected void subscribeTicker() {
        getAssets(true).stream().forEach(base -> getAllStablecoinQuotesExceptBusd(true).forEach(quote -> this.sendMessage(
                "{\"id\":ID,\"method\":\"subscribe\",\"topics\":[\"spot/ticker:BASE_QUOTE\"]}"
                        .replace("ID", incAndGetIdAsString())
                        .replace("BASE", base)
                        .replace("QUOTE", quote))));
    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("spot/ticker:")) {
            return Optional.empty();
        }

        var tickerUpdate = objectMapper.readValue(message, TickerUpdate.class);
        var pair = SymbolHelper.getPair(tickerUpdate.getTopic().replace("spot/ticker:", ""));

        var ticker = Ticker.builder().source(Source.WS).exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(Double.parseDouble(tickerUpdate.getData().getLast_trade())).timestamp(currentTimestamp()).build();

        return Optional.of(Collections.singletonList(ticker));
    }
}
