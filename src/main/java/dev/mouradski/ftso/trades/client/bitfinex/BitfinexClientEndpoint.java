package dev.mouradski.ftso.trades.client.bitfinex;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@ClientEndpoint
public class BitfinexClientEndpoint extends AbstractClientEndpoint {

    private Map<Double, Pair<String, String>> channelIds = new HashMap<>();

    @Override
    protected String getUri() {
        return "wss://api-pub.bitfinex.com/ws/2";
    }

    @Override
    protected void subscribe() {
        getAssets().stream().map(String::toUpperCase)
                .forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> this
                        .sendMessage("{\"event\":\"subscribe\", \"channel\":\"trades\",\"symbol\":\"tSYMBOLQUOTE\"}"
                                .replace("SYMBOL", base).replace("QUOTE", quote))));
    }

    @Override
    protected String getExchange() {
        return "bitfinex";
    }

    @Override
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {
        if (!message.contains("te")) {
            return Optional.empty();
        }

        var messageArray = gson.fromJson(message, Object[].class);

        var channelId = (Double) messageArray[0];
        var tradeData = (List<Double>) messageArray[2];

        var pair = channelIds.get(channelId);

        return Optional.of(Collections.singletonList(Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight())
                .price(tradeData.get(3)).amount(Math.abs(tradeData.get(2))).timestamp(currentTimestamp())
                .build()));
    }

    @Override
    protected void decodeMetadata(String message) {
        if (message.contains("subscribed")) {
            var symbolId = message.split("\"pair\":\"")[1].split("\"")[0].replace("t", "");
            var channelId = Double.valueOf(message.split("\"chanId\":")[1].split(",")[0]);
            var pair = SymbolHelper.getPair(symbolId);
            this.channelIds.put(channelId, pair);
        }
    }
}
