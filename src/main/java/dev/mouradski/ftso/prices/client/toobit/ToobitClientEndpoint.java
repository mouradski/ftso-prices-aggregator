package dev.mouradski.ftso.prices.client.toobit;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
@ClientEndpoint
@Startup
public class ToobitClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://stream.toobit.com/quote/ws/v1";
    }

    @Override
    protected void subscribeTicker() {
        var pairs = getAssets(true).stream().map(v -> v + "USDT").collect(Collectors.joining(","));
        this.sendMessage("{\"symbol\": \"PAIRS\",\"topic\": \"realtimes\",\"event\": \"sub\",\"params\":{\"binary\": false}}".replace("PAIRS", pairs));
    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("realtimes")) {
            return Optional.empty();
        }

        var tickerData = objectMapper.readValue(message, TickerData.class);

        var pair = SymbolHelper.getPair(tickerData.getSymbol());


        return Optional.of(Collections.singletonList(Ticker.builder().source(Source.WS).exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(tickerData.getData().get(0).getC()).timestamp(currentTimestamp()).build()));
    }

    @Override
    protected String getExchange() {
        return "toobit";
    }

    @Scheduled(every="60s")
    public void ping() {
        this.sendMessage("{\"ping\": ID}".replace("ID", new Date().getTime() + ""));
    }

}
