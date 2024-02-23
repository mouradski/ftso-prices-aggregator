package dev.mouradski.ftso.prices.client.kraken;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
@ClientEndpoint
@Startup
public class KrakenClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("ticker") || message.contains("systemStatus") || message.contains("errorMessage")
                || message.contains("subscriptionStatus")) {
            return Optional.empty();
        }

        var jsonArray = new JSONArray(message);

       	var pair = SymbolHelper.getPair(jsonArray.getString(3).replace("XBT", "BTC").replace("XDG", "DOGE"));


        var details = jsonArray.getJSONObject(1);
        var lArray = details.getJSONArray("c");
        var lastPrice = Double.valueOf(lArray.getString(0));

        return Optional.of(Collections.singletonList(Ticker.builder().source(Source.WS).exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(lastPrice).timestamp(currentTimestamp()).build()));
    }

    @Override
    protected String getUri() {
        return "wss://ws.kraken.com";
    }

    @Override
    protected void subscribeTicker() {
        List<String> paris = new ArrayList<>();

        getAssets(true).forEach(
                base -> getAllQuotesExceptBusd(true).forEach(quote -> paris.add("\"" + base + "/" + quote + "\"")));

        this.sendMessage("{\"event\":\"subscribe\", \"pair\":[" + paris.stream().collect(Collectors.joining(","))
                + "], \"subscription\":{\"name\":\"ticker\"}}");
    }

    @Override
    protected String getExchange() {
        return "kraken";
    }

}
