package dev.mouradski.ftso.prices.client.hashkey;

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
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@ClientEndpoint
@Startup
public class HashkeyClientEndpoint extends AbstractClientEndpoint {
    @Override
    protected String getUri() {
        return "wss://stream-pro.hashkey.com/quote/ws/v1";
    }

    @Override
    protected String getExchange() {
        return "hashkey";
    }

    @Override
    protected void subscribeTicker() {
        getAssets(true).forEach(base -> {
            getAllQuotes(true).forEach(quote -> {
                sendMessage("{\"symbol\": \"SYMBOL\",\"topic\": \"realtimes\",\"event\": \"sub\",\"params\":{\"binary\": \"False\"},\"id\": ID }".replace("SYMBOL", base + quote).replace("ID", incAndGetIdAsString()));
            });
        });
    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("realtimes")) {
            return Optional.empty();
        }

        var tickerData = objectMapper.readValue(message, MarketData.class);

        var pair = SymbolHelper.getPair(tickerData.getSymbol());

        var tickers = new ArrayList<Ticker>();

        tickerData.getData().forEach(data -> {
            tickers.add(Ticker.builder().source(Source.WS).exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(Double.parseDouble(data.getC())).timestamp(currentTimestamp()).build());
        });

        return Optional.of(tickers);
    }


    @Scheduled(every = "10s")
    public void ping() {
        sendMessage("{\"ping\":ID}".replace("ID", String.valueOf(currentTimestamp())));
    }
}
