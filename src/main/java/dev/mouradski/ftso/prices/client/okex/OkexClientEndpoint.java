package dev.mouradski.ftso.prices.client.okex;

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
public class OkexClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://ws.okx.com:8443/ws/v5/public";
    }

    @Override
    protected void subscribeTicker() {

        var subscriptionMsg = new StringBuilder("{\"op\": \"subscribe\",\"args\": [");

        var channels = new ArrayList<String>();

        getAssets(true).forEach(base -> getAllQuotes(true).forEach(quote -> channels.add("{\"channel\": \"index-tickers\",\"instId\": \"SYMBOL-QUOTE\"}".replace("SYMBOL", base).replace("QUOTE", quote))));

        subscriptionMsg.append(channels.stream().collect(Collectors.joining(",")));
        subscriptionMsg.append("]}");

        this.sendMessage(subscriptionMsg.toString());

    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("index-tickers")) {
            return Optional.empty();
        }

        var tickerInfo = objectMapper.readValue(message, TickerInfo.class);

        var pair = SymbolHelper.getPair(tickerInfo.getArg().getInstId());

        return Optional.of(Collections.singletonList(Ticker.builder().source(Source.WS).exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(tickerInfo.getData().get(0).getIdxPx()).timestamp(currentTimestamp()).build()));
    }

    @Override
    protected String getExchange() {
        return "okex";
    }

    @Scheduled(every="30s")
    public void ping() {
        this.sendMessage("ping");
    }
}
