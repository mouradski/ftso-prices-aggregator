package dev.mouradski.ftso.trades.client.gateio;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonParser;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Ticker;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@ClientEndpoint
@Startup
public class GateIOClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {

        if (!message.contains("spot.trades") || message.contains("status")) {
            return Optional.empty();
        }


        var jelement = new JsonParser().parse(message);
        var jobject = jelement.getAsJsonObject();
        var result = jobject.getAsJsonObject("result");

        var gateIOTrade = gson.fromJson(result, GateIOTrade.class);

        return Optional.of(Collections.singletonList(Trade.builder().exchange(getExchange()).base(gateIOTrade.getCurrencyPair().split("_")[0])
                .quote(gateIOTrade.getCurrencyPair().split("_")[1]).price(gateIOTrade.getPrice()).amount(gateIOTrade.getAmount()).timestamp(currentTimestamp()).build()));


    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("spot.tickers") || message.contains("status")) {
            return Optional.empty();
        }

        var gateIoTicker = objectMapper.readValue(message, GeteIOTicker.class);

        var par = SymbolHelper.getPair(gateIoTicker.getResult().getCurrencyPair());

        return Optional.of(Collections.singletonList(Ticker.builder().exchange(getExchange()).base(par.getLeft()).quote(par.getRight()).lastPrice(gateIoTicker.getResult().getLast()).timestamp(currentTimestamp()).build()));
    }

    @Override
    protected String getUri() {
        return "wss://api.gateio.ws/ws/v4/";
    }

    @Override
    protected void subscribeTrade() {

        var pairs = new ArrayList<String>();

        getAssets().stream().filter(v -> !"dgb".equals(v) && !v.startsWith("usd") && !v.equals("busd"))
                .forEach(base -> pairs.add("\"" + base.toUpperCase() + "_" + "USDT\""));


        var timestamp = System.currentTimeMillis();
        var subscribeMessage = String.format(
                "{\"time\": %d, \"channel\": \"spot.trades\", \"event\": \"subscribe\", \"payload\": [%s]}", timestamp,
                String.join(",", pairs));

        this.sendMessage(subscribeMessage);
    }

    @Override
    protected void subscribeTicker() {

        var pairs = new ArrayList<String>();

        getAssets().stream().filter(v -> !"dgb".equals(v) && !v.startsWith("usd") && !v.equals("busd"))
                .forEach(base -> pairs.add("\"" + base.toUpperCase() + "_" + "USDT\""));


        var timestamp = System.currentTimeMillis();
        var subscribeMessage = String.format(
                "{\"time\": %d, \"channel\": \"spot.tickers\", \"event\": \"subscribe\", \"payload\": [%s]}", timestamp,
                String.join(",", pairs));

        this.sendMessage(subscribeMessage);
    }

    @Override
    protected String getExchange() {
        return "gateio";
    }

    @Scheduled(every="30s")
    public void ping() {
        this.sendMessage("{\"method\":\"server.ping\"}");
    }
}
