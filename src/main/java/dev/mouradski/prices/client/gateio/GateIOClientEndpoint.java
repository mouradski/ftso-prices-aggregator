package dev.mouradski.prices.client.gateio;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonParser;
import dev.mouradski.prices.client.AbstractClientEndpoint;
import dev.mouradski.prices.model.Trade;
import dev.mouradski.prices.service.PriceService;
import jakarta.websocket.ClientEndpoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ClientEndpoint
@Component
public class GateIOClientEndpoint extends AbstractClientEndpoint {

    public GateIOClientEndpoint(PriceService priceSender, @Value("${exchanges}") List<String> exchanges, @Value("${assets}") List<String> assets) {
        super(priceSender, exchanges, assets);
    }

    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {

        if (!message.contains("spot.trades") || message.contains("status")) {
            return new ArrayList<>();
        }

        var jelement = new JsonParser().parse(message);
        var jobject = jelement.getAsJsonObject();
        var result = jobject.getAsJsonObject("result");

        var gateIOTrade = gson.fromJson(result, GateIOTrade.class);

        return Arrays.asList(Trade.builder().exchange(getExchange()).symbol(gateIOTrade.getCurrencyPair().split("_")[0])
                .quote(gateIOTrade.getCurrencyPair().split("_")[1]).price(gateIOTrade.getPrice()).amount(gateIOTrade.getAmount()).build());

    }

    @Override
    protected String getUri() {
        return "wss://api.gateio.ws/ws/v4/";
    }

    @Override
    protected void subscribe() {

        var pairs = new ArrayList<String>();

        getAssets().stream().filter(v -> !v.startsWith("usd") && !v.equals("busd")).forEach(symbol -> {
            pairs.add("\"" + symbol.toUpperCase() + "_" + "USDT\"");
        });


        var timestamp = System.currentTimeMillis();
        var subscribeMessage = String.format(
                "{\"time\": %d, \"channel\": \"spot.trades\", \"event\": \"subscribe\", \"payload\": [%s]}", timestamp,
                pairs.stream().collect(Collectors.joining(",")));

        this.sendMessage(subscribeMessage);
    }

    @Override
    protected String getExchange() {
        return "gateio";
    }

    @Scheduled(fixedDelay = 30000)
    public void ping() {
        this.sendMessage("{\"method\":\"server.ping\"}");
    }
}
