package dev.mouradski.ftso.prices.client.koinbay;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@ClientEndpoint
@Startup
public class KoinbayClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://ws.koinbay.com/kline-api/ws";
    }

    @Override
    protected String getExchange() {
        return "koinbay";
    }

    @Override
    protected void subscribeTicker() {

        var subscribeTemplate = """
                        {
                            "event":"sub",
                            "params":{
                                "channel":"market_%s_ticker",
                                "cb_id":"1"
                            }
                        }
                        """;

        getAssets(false).forEach(base -> {
            getAllQuotes(false).forEach(quote -> {
                var pair = base + quote;
                this.sendMessage(subscribeTemplate.formatted(pair));
            });
        });
    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("_ticker")) {
            return Optional.empty();
        }

        var tickerMsg = objectMapper.readValue(message, TickerMsg.class);

        var pair = SymbolHelper.getPair(tickerMsg.getChannel().split("_")[1]);
        return Optional.of(List.of(Ticker.builder()
                .source(Source.WS)
                .exchange(getExchange())
                .base(pair.getLeft())
                .quote(pair.getRight())
                .lastPrice(Double.parseDouble(tickerMsg.getTick().getClose()))
                .timestamp(currentTimestamp())
                .build()));
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
