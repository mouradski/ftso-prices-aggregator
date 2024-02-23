package dev.mouradski.ftso.prices.client.huobi;

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
public class HuobiClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://api.huobi.pro/ws";
    }

    @Override
    protected void subscribeTicker() {
        getAssets().forEach(symbol -> getAllQuotesExceptBusd(false).forEach(base -> this.sendMessage("{   \"sub\": \"market." + symbol + base + ".ticker\",   \"id\": \"ID\" }".replace("ID", new Date().getTime() + ""))));
    }

    @Override
    protected String getExchange() {
        return "huobi";
    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("ticker") || !message.contains("open")) {
            return Optional.empty();
        }

        var tickerMessage = gson.fromJson(message, TickerResponse.class);


        var symbolId = tickerMessage.getCh().split("\\.")[1].toUpperCase();

        var par = SymbolHelper.getPair(symbolId);

        return Optional.of(Collections.singletonList(Ticker.builder().source(Source.WS).exchange(getExchange()).base(par.getLeft()).quote(par.getRight()).lastPrice(tickerMessage.getTick().getLastPrice()).timestamp(currentTimestamp()).build()));
    }

    @Override
    protected boolean pong(String message) {
        if (message.contains("ping")) {
            this.sendMessage(message.replace("ping", "pong"));
            return true;
        }

        return false;
    }
}
