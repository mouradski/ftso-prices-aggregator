package dev.mouradski.ftsopriceclient.client.liquid;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftsopriceclient.client.AbstractClientEndpoint;
import dev.mouradski.ftsopriceclient.model.Trade;
import dev.mouradski.ftsopriceclient.service.PriceService;
import jakarta.websocket.ClientEndpoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Stream;


@ClientEndpoint
@Component
public class LiquidClientEndpoint extends AbstractClientEndpoint {

    private Map<String, String> produitsMap;

    protected LiquidClientEndpoint(PriceService priceSender, @Value("${exchanges}") List<String> exchanges, @Value("${assets}") List<String> assets) {
        super(priceSender, exchanges, assets);
    }

    @Override
    protected String getUri() {
        return "wss://tap.liquid.com/app/LiquidTapClient";
    }

    @Override
    protected void prepareConnection() {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.liquid.com/products"))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            LiquidProduit[] liquidProduits = this.objectMapper.readValue(response.body(), LiquidProduit[].class);

            produitsMap = new HashMap<>();

            Stream.of(liquidProduits)
                    .filter(v -> getAssets().contains(v.getBaseCurrency().toLowerCase()))
                    .filter(v -> Arrays.asList("USD", "USDC", "USDT").contains(v.getQuotedCurrency()))
                    .forEach(produit -> {

                        produitsMap.put(produit.currencyPairCode, produit.getId());
                    });


        } catch (IOException | InterruptedException e) {
        }
    }

    @Override
    protected void subscribe() {
        produitsMap.entrySet().forEach(e -> {
            var channel = "product_cash_" + e.getKey() + "_" + e.getValue();
            this.sendMessage("{\"event\":\"pusher:subscribe\",\"data\":{\"channel\":\"CHANNEL\"}}".replace("CHANNEL", channel));
        });
    }

    @Override
    protected String getExchange() {
        return "liquid";
    }

    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {
        return new ArrayList<>();
    }

    @Override
    protected long getTimeout() {
        return 300;
    }
}
