package dev.mouradski.ftso.prices.client.bitfinex;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;

import java.util.*;

@ApplicationScoped
@ClientEndpoint
@Startup
public class BitfinexClientEndpoint extends AbstractClientEndpoint {

    private Map<Double, Pair<String, String>> channelIds = new HashMap<>();

    @Override
    protected String getUri() {
        return "wss://api-pub.bitfinex.com/ws/2";
    }

    @Override
    protected void subscribeTicker() {
        getAssets().stream().map(String::toUpperCase)
                .forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> {
                    var symbol = "t" + base + ("USDT".equals(quote) ? "UST" : ("USDC".equals(quote) ? "UDC" : quote));
                    var symbol2 = "t" + base + ("USDT".equals(quote) ? "UST" : ("USDC".equals(quote) ? "UDC" : quote));
                    this
                            .sendMessage(
                                    "{\"event\":\"subscribe\", \"channel\":\"ticker\",\"symbol\":\"" + symbol + "\"}");
                    this
                            .sendMessage(
                                    "{\"event\":\"subscribe\", \"channel\":\"ticker\",\"symbol\":\"" + symbol2 + "\"}");
                }));
    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {

        if (message.contains("hb")) {
            return Optional.empty();
        }

        var jsonArray = new JSONArray(message);

        var channelId = jsonArray.getDouble(0);

        var pair = channelIds.get(channelId);

        if (pair == null) {
            return Optional.empty();
        }

        var quote = pair.getRight();

        var tickerData = jsonArray.getJSONArray(1);

        double lastPrice = tickerData.getDouble(6);

        return Optional.of(Collections.singletonList(Ticker.builder().exchange(getExchange()).base(pair.getLeft())
                .quote(quote).lastPrice(lastPrice).timestamp(currentTimestamp()).build()));
    }

    @Override
    protected String getExchange() {
        return "bitfinex";
    }

    @Override
    protected void decodeMetadata(String message) {
        if (message.contains("subscribed")) {
            var symbolId = message.split("\"pair\":\"")[1].split("\"")[0].replace("t", "").replace("UST", "USDT")
                    .replace("UDC", "USDC")
                    .replace(":", "");
            var channelId = Double.valueOf(message.split("\"chanId\":")[1].split(",")[0]);
            var pair = SymbolHelper.getPair(symbolId);
            this.channelIds.put(channelId, pair);
        }
    }
}
