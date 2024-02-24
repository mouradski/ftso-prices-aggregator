package dev.mouradski.ftso.prices.client.bitrue;

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
public class BitrueClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://ws.bitrue.com/kline-api/ws";
    }

    @Override
    protected void subscribeTicker() {
        getAssets().stream().forEach(base -> getAllStablecoinQuotesExceptBusd(false).forEach(quote -> this.sendMessage(
                "{\"event\":\"sub\",\"params\":{\"cb_id\":\"CB_ID\",\"channel\":\"market_CB_ID_ticker\"}}"
                        .replaceAll("CB_ID", base + quote))));
    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("close")) {
            return Optional.empty();
        }

        var tickerResponse = objectMapper.readValue(message, TickerResponse.class);

        var pair = SymbolHelper
                .getPair(tickerResponse.getChannel().replace("market_", "").replace("_ticker", ""));

        return Optional.of(Collections
                .singletonList(Ticker.builder().source(Source.WS).exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight())
                        .lastPrice(tickerResponse.getTick().getClose()).timestamp(currentTimestamp()).build()));
    }

    @Override
    protected String getExchange() {
        return "bitrue";
    }

    @Override
    protected long getTimeout() {
        return 30;
    }

    @Override
    protected boolean pong(String message) {

        if (message.contains("ping")) {
            this.sendMessage(message.replaceAll("ping", "pong"));
            return true;
        }

        return false;
    }
}
