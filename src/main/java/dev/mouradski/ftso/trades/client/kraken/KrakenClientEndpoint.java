package dev.mouradski.ftso.trades.client.kraken;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonArray;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import jakarta.websocket.ClientEndpoint;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ClientEndpoint
@Component
public class KrakenClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {
        if (!message.contains("trade") || message.contains("systemStatus") || message.contains("errorMessage")
                || message.contains("subscriptionStatus")) {
            return Optional.empty();
        }

        var trades = new ArrayList<Trade>();

        var array = gson.fromJson(message, JsonArray.class);

        // check if it's a trade message
        if (array.get(array.size() - 2).getAsString().equals("trade")) {
            // get the trade data
            var tradesData = array.get(1).getAsJsonArray();

            var pair = array.get(array.size() - 1).getAsString();

            var base = pair.split("/")[0].replace("XBT", "BTC").replace("XDG", "DOGE");
            var quote = pair.split("/")[1];

            for (var tradeData : tradesData) {
                var tradeArray = tradeData.getAsJsonArray();
                trades.add(Trade.builder().exchange(getExchange()).base(base).quote(quote)
                        .price(tradeArray.get(0).getAsDouble()).amount(tradeArray.get(1).getAsDouble())
                        .timestamp(currentTimestamp()) // timestamp sent in seconds
                        .build());
            }
        }

        return Optional.of(trades);
    }

    @Override
    protected String getUri() {
        return "wss://ws.kraken.com";
    }

    @Override
    protected void subscribe() {
        List<String> paris = new ArrayList<>();

        getAssets(true).forEach(
                base -> getAllQuotesExceptBusd(true).forEach(quote -> paris.add("\"" + base + "/" + quote + "\"")));

        this.sendMessage("{\"event\":\"subscribe\", \"pair\":[" + paris.stream().collect(Collectors.joining(","))
                + "], \"subscription\":{\"name\":\"trade\"}}");
    }

    @Override
    protected String getExchange() {
        return "kraken";
    }

}
