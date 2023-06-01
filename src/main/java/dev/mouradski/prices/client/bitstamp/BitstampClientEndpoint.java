package dev.mouradski.prices.client.bitstamp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonParser;
import dev.mouradski.prices.client.AbstractClientEndpoint;
import dev.mouradski.prices.model.Trade;
import dev.mouradski.prices.service.PriceService;
import dev.mouradski.prices.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ClientEndpoint
@Component
public class BitstampClientEndpoint extends AbstractClientEndpoint {

    public BitstampClientEndpoint(PriceService priceSender, @Value("${exchanges}") List<String> exchanges, @Value("${assets}") List<String> assets) {
        super(priceSender, exchanges, assets);
    }


    @Override
    protected String getUri() {
        return "wss://ws.bitstamp.net";
    }


    @Override
    protected void subscribe() {
        getAssets().forEach(symbol -> {
            getAllQuotesExceptBusd(false).forEach(quote -> {
                this.sendMessage("{\"event\": \"bts:subscribe\", \"data\": {\"channel\": \"live_trades_" + symbol + quote + "\"}}");
            });
        });
    }

    @Override
    protected String getExchange() {
        return "bitstamp";
    }


    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {
        if (message.contains("\"event\":\"trade\"")) {
            // parse message as JSON
            var jsonObject = JsonParser.parseString(message).getAsJsonObject();

            // extract pair from channel name
            var channelName = jsonObject.get("channel").getAsString();
            var pair = channelName.substring(12).toUpperCase();  // remove "live_trades_" and convert to upper case

            // extract trade data
            var tradeData = jsonObject.get("data").getAsJsonObject();

            // create Trade object
            var trade = new Trade();
            trade.setPrice(tradeData.get("price").getAsDouble());
            trade.setAmount(tradeData.get("amount").getAsDouble());
            trade.setExchange(getExchange());

            Pair<String, String> symbol = SymbolHelper.getSymbol(pair);


            trade.setSymbol(symbol.getLeft());
            trade.setQuote(symbol.getRight());

            return Arrays.asList(trade);
        } else {
            return new ArrayList<>();
        }
    }

}
