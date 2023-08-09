package dev.mouradski.ftso.trades.client.crypto;


import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@ClientEndpoint

public class CryptoComClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://stream.crypto.com/v2/market";
    }

    @Override
    protected void subscribe() {
        getAssets().stream().filter(v -> !v.equals("usdt")).map(String::toUpperCase).forEach(base -> {
            this.sendMessage("{\"id\": " + incAndGetId() + ",\"method\": \"subscribe\",\"params\": {\"channels\": [\"trade." + base + "_USDT\"]},\"nonce\": " + new Date().getTime() + "}");
            this.sendMessage("{\"id\": " + incAndGetId() + ",\"method\": \"subscribe\",\"params\": {\"channels\": [\"trade." + base + "_USD\"]},\"nonce\": " + new Date().getTime() + "}");
        });

    }

    @Override
    protected String getExchange() {
        return "crypto";
    }

    @Override
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {

        if (!message.contains("result")) {
            return Optional.empty();
        }

        var response = gson.fromJson(message, Response.class);

        var trades = new ArrayList<Trade>();

        var base = response.getResult().getSubscription().replace("trade.", "").split("_")[0];
        var quote = response.getResult().getSubscription().replace("trade.", "").split("_")[1];

        response.getResult().getData().forEach(cryptoTrade -> {
            trades.add(Trade.builder().exchange(getExchange()).base(base).quote(quote).price(cryptoTrade.getP()).amount(cryptoTrade.getQ()).timestamp(currentTimestamp()).build());
        });


        return Optional.of(trades);
    }

    @Override
    protected boolean pong(String message) {
        if (message.contains("public/heartbeat")) {
            this.sendMessage(message.replace("public/heartbeat", "public/respond-heartbeat"));
            return true;
        }

        return false;
    }
}
