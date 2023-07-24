package dev.mouradski.ftso.trades.client.bitstamp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonParser;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.service.TradeService;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ClientEndpoint
@Component
public class BitstampClientEndpoint extends AbstractClientEndpoint {

    public BitstampClientEndpoint(TradeService priceSender, @Value("${exchanges}") List<String> exchanges,
            @Value("${assets}") List<String> assets) {
        super(priceSender, exchanges, assets);
    }

    @Override
    protected String getUri() {
        return "wss://ws.bitstamp.net";
    }

    @Override
    protected void subscribe() {
        getAssets().forEach(base -> getAllQuotesExceptBusd(false).forEach(quote -> this.sendMessage(
                "{\"event\": \"bts:subscribe\", \"data\": {\"channel\": \"live_trades_" + base + quote + "\"}}")));
    }

    @Override
    protected String getExchange() {
        return "bitstamp";
    }

    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {
        if (message.contains("\"event\":\"trade\"")) {
            var jsonObject = JsonParser.parseString(message).getAsJsonObject();

            var channelName = jsonObject.get("channel").getAsString();
            var symbolId = channelName.substring(12).toUpperCase(); // remove "live_trades_" and convert to upper case

            var tradeData = jsonObject.get("data").getAsJsonObject();

            var trade = new Trade();
            trade.setPrice(tradeData.get("price").getAsDouble());
            trade.setAmount(tradeData.get("amount").getAsDouble());
            trade.setTimestamp(tradeData.get("timestamp").getAsLong() * 1000); // timestamp is sent in seconds
            trade.setExchange(getExchange());

            var pair = SymbolHelper.getPair(symbolId);

            trade.setBase(pair.getLeft());
            trade.setQuote(pair.getRight());

            return Arrays.asList(trade);
        } else {
            return new ArrayList<>();
        }
    }

}
