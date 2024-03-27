package dev.mouradski.ftso.prices.client.bitforex;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@ClientEndpoint
@Startup
public class BitforexClientEndpoint extends AbstractClientEndpoint {

    private Set<String> processedIds = new HashSet<>();

    @Override
    protected String getUri() {
        return "wss://www.bitforex.com/mkapi/coinGroup1/ws";
    }

    @Override
    protected void subscribeTicker() {
        getAssets(false).forEach(base -> {
            getAllQuotes(false).forEach(quote -> {
                this.sendMessage("[{     \"type\": \"subHq\",     \"event\": \"ticker\",     \"param\": {         \"businessType\": \"coin-QUOTE-BASE\",         \"size\": 20     } }]".replace("BASE", base).replace("QUOTE", quote));
            });
        });
    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("24H")) {
            return Optional.empty();
        }

        var tickerEvent = objectMapper.readValue(message, TickerEvent.class);

        var pair = SymbolHelper.getPair(tickerEvent.getParam().getBusinessType().replace("coin-", ""));

        return Optional.of(Collections.singletonList(Ticker.builder().source(Source.WS).exchange(getExchange()).base(pair.getRight()).quote(pair.getLeft()).lastPrice(tickerEvent.getData().getLast()).timestamp(currentTimestamp()).build()));
    }

    @Override
    protected String getExchange() {
        return "bitforex";
    }

    @Scheduled(every="20s")
    public void ping() {
        this.sendMessage("ping_p");
    }

    @Scheduled(every="600s")
    public void purgeIds() {
        processedIds = processedIds.stream().sorted(Comparator.reverseOrder()).limit(100).collect(Collectors.toSet());
    }
}
