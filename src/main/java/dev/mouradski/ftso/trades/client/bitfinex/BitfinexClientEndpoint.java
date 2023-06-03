package dev.mouradski.ftso.trades.client.bitfinex;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.service.PriceService;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@ClientEndpoint
public class BitfinexClientEndpoint extends AbstractClientEndpoint {

    private Map<Double, Pair<String, String>> channelIds = new HashMap<>();

    public BitfinexClientEndpoint(PriceService priceSender, @Value("${exchanges}") List<String> exchanges, @Value("${assets}") List<String> assets) {
        super(priceSender, exchanges, assets);
    }

    @Override
    protected String getUri() {
        return "wss://api-pub.bitfinex.com/ws/2";
    }

    @Override
    protected void subscribe() {
        getAssets().stream().map(String::toUpperCase).forEach(symbol -> {
            getAllQuotesExceptBusd(true).forEach(quote -> {
                this.sendMessage("{\"event\":\"subscribe\", \"channel\":\"trades\",\"symbol\":\"tSYMBOLQUOTE\"}".replace("SYMBOL", symbol).replace("QUOTE", quote));
            });
        });
    }

    @Override
    protected String getExchange() {
        return "bitfinex";
    }

    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {
        if (!message.contains("te")) {
            return new ArrayList<>();
        }

        var messageArray = gson.fromJson(message, Object[].class);

        var channelId = (Double) messageArray[0];
        var tradeData = (List<Double>) messageArray[2];

        Pair<String, String> symbol = channelIds.get(channelId);

        return Arrays.asList(Trade.builder().exchange(getExchange()).symbol(symbol.getLeft()).quote(symbol.getRight()).price(tradeData.get(3)).amount(Math.abs(tradeData.get(2))).build());
    }

    @Override
    protected void decodeMetadata(String message) {
        if (message.contains("subscribed")) {
            var symbol = message.split("\"pair\":\"")[1].split("\"")[0].replace("t", "");
            var channelId = Double.valueOf(message.split("\"chanId\":")[1].split(",")[0]);
            var symbolPair = SymbolHelper.getSymbol(symbol);
            this.channelIds.put(channelId, symbolPair);
        }
    }
}
