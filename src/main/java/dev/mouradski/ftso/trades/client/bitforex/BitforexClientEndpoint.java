package dev.mouradski.ftso.trades.client.bitforex;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@ClientEndpoint
public class BitforexClientEndpoint extends AbstractClientEndpoint {

    private Set<String> processedIds = new HashSet<>();

    @Override
    protected String getUri() {
        return "wss://www.bitforex.com/mkapi/coinGroup1/ws";
    }

    @Override
    protected void subscribe() {
        getAssets(false).forEach(base -> {
            getAllQuotesExceptBusd(false).forEach(quote -> {
                this.sendMessage("[{     \"type\": \"subHq\",     \"event\": \"trade\",     \"param\": {         \"businessType\": \"coin-QUOTE-BASE\",         \"size\": 20     } }]".replace("BASE", base).replace("QUOTE", quote));
            });
        });
    }

    @Override
    protected String getExchange() {
        return "bitforex";
    }


    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {
        if (!message.contains("businessType")) {
            return new ArrayList<>();
        }

        var trades = new ArrayList<Trade>();

        var tradeEvent = this.objectMapper.readValue(message, TradeEvent.class);

        var pair = SymbolHelper.getPair(tradeEvent.getParam().getBusinessType().replace("coin-", ""));


        tradeEvent.getData().stream().filter(data -> !processedIds.contains(data.getTransactionId())).forEach(data -> {
            	processedIds.add(data.getTransactionId());
                trades.add(Trade.builder().exchange(getExchange()).base(pair.getRight()).quote(pair.getLeft()).amount(data.getAmount()).price(data.getPrice()).timestamp(currentTimestamp()).build());
        });

        return trades;
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
