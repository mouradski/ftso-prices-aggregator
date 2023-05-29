package dev.mouradski.ftsopriceclient.client.kraken;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import dev.mouradski.ftsopriceclient.utils.Constants;
import dev.mouradski.ftsopriceclient.client.AbstractClientEndpoint;
import dev.mouradski.ftsopriceclient.model.Trade;
import dev.mouradski.ftsopriceclient.service.PriceService;
import jakarta.websocket.ClientEndpoint;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ClientEndpoint
@Component
public class KrakenClientEndpoint extends AbstractClientEndpoint {

    public KrakenClientEndpoint(PriceService priceSender) {
        super(priceSender);
    }

    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {
        if (!message.contains("trade") || message.contains("systemStatus") || message.contains("errorMessage") || message.contains("subscriptionStatus")) {
            return new ArrayList<>();
        }

        var trades = new ArrayList<Trade>();

        var array = new Gson().fromJson(message, JsonArray.class);

        // check if it's a trade message
        if (array.get(array.size() - 2).getAsString().equals("trade")) {
            // get the trade data
            var tradesData = array.get(1).getAsJsonArray();

            var pair = array.get(array.size() - 1).getAsString();

            var symbol = pair.split("/")[0].replace("XBT", "BTC");
            var quote = pair.split("/")[1];

            for (var tradeData : tradesData) {
                var tradeArray = tradeData.getAsJsonArray();
                trades.add(Trade.builder().exchange(getExchange()).symbol(symbol).quote(quote).price(tradeArray.get(0).getAsDouble()).amount(tradeArray.get(1).getAsDouble()).build());
            }
        }

        return trades;
    }

    @Override
    protected String getUri() {
        return "wss://ws.kraken.com";
    }

    @Override
    protected void subscribe() {
        List<String> paris = new ArrayList<>();

        Constants.SYMBOLS.forEach(symbol -> {
            paris.add("\"" + symbol.toUpperCase() + "/USD\"");
            paris.add("\"" + symbol.toUpperCase() + "/USDT\"");
            paris.add("\"" + symbol.toUpperCase() + "/USDC\"");
        });

        this.sendMessage("{\"event\":\"subscribe\", \"pair\":[" + paris.stream().collect(Collectors.joining(",")) + "], \"subscription\":{\"name\":\"trade\"}}");
    }

    @Override
    protected String getExchange() {
        return "kraken";
    }

}
