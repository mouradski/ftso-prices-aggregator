package dev.mouradski.ftso.trades.client.crypto;


import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.service.TradeService;
import jakarta.websocket.ClientEndpoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ClientEndpoint
@Component
public class CryptoComClientEndpoint extends AbstractClientEndpoint {

    protected CryptoComClientEndpoint(TradeService priceSender, @Value("${exchanges}") List<String> exchanges, @Value("${assets}") List<String> assets) {
        super(priceSender, exchanges, assets);
    }

    @Override
    protected String getUri() {
        return "wss://stream.crypto.com/v2/market";
    }

    @Override
    protected void subscribe() {
        getAssets().stream().filter(v -> !v.equals("usdt")).map(String::toUpperCase).forEach(base -> {
            this.sendMessage("{\"id\": " + counter.getCount() + ",\"method\": \"subscribe\",\"params\": {\"channels\": [\"trade." + base + "_USDT\"]},\"nonce\": " + new Date().getTime() + "}");
            this.sendMessage("{\"id\": " + counter.getCount() + ",\"method\": \"subscribe\",\"params\": {\"channels\": [\"trade." + base + "_USD\"]},\"nonce\": " + new Date().getTime() + "}");
        });

    }

    @Override
    protected String getExchange() {
        return "crypto";
    }

    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {

        if (!message.contains("result")) {
            return new ArrayList();
        }

        var response = gson.fromJson(message, Response.class);

        var trades = new ArrayList<Trade>();

        var base = response.getResult().getSubscription().replace("trade.", "").split("_")[0];
        var quote = response.getResult().getSubscription().replace("trade.", "").split("_")[1];

        response.getResult().getData().forEach(cryptoTrade -> {
            trades.add(Trade.builder().exchange(getExchange()).base(base).quote(quote).price(cryptoTrade.getP()).amount(cryptoTrade.getQ()).build());
        });


        return trades;
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
