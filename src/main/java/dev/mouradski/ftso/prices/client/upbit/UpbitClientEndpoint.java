package dev.mouradski.ftso.prices.client.upbit;

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

@ApplicationScoped
@ClientEndpoint
@Startup
public class UpbitClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://api.upbit.com/websocket/v1";
    }

    @Override
    protected void subscribeTicker() {
        var pairs = new ArrayList<String>();
        getAssets(true).forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> pairs.add("\"" + quote + "-" + base + "\"")));
        this.sendMessage("[{\"ticket\":\"tickers\"},{\"type\":\"ticker\",\"codes\":[SYMBOL]}]".replace("SYMBOL", String.join(",", pairs)));
    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("ticker")) {
            return Optional.empty();
        }

        var tickerData = objectMapper.readValue(message, UpbitTicker.class);

        var pair = SymbolHelper.getPair(tickerData.getCode());

        var ticker = Ticker.builder().source(Source.WS).exchange(getExchange()).lastPrice(tickerData.getLastPrice()).timestamp(currentTimestamp()).build();

        if (pair.getLeft().startsWith("USD") && !pair.getRight().startsWith("USD")) {
            ticker.setBase(pair.getRight());
            ticker.setQuote(pair.getLeft());
        } else {
            ticker.setBase(pair.getLeft());
            ticker.setQuote(pair.getRight());
        }

        return Optional.of(Collections.singletonList(ticker));
    }

    @Override
    protected String getExchange() {
        return "upbit";
    }

    @Scheduled(every="100s")
    public void ping() {
        this.sendMessage("PING");
    }
}
