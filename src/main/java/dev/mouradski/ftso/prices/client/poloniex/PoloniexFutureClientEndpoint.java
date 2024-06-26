package dev.mouradski.ftso.prices.client.poloniex;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@ClientEndpoint
@Startup
public class PoloniexFutureClientEndpoint extends AbstractClientEndpoint {
    @Override
    protected String getUri() {
        return generateUri();
    }

    private String generateUri() {
        return "wss://futures-apiws.poloniex.com/endpoint?token=" + generateToken();
    }

    private String generateToken() {
        try {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://futures-api.poloniex.com/api/v1/bullet-public"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            var tokenResponse = gson.fromJson(response.body(), TokenResponse.class);

           return tokenResponse.getData().getToken();

        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    protected String getExchange() {
        return "poloniexfuture";
    }

    @Override
    protected void subscribeTicker() {
        var subscribeMsgTemplate = "{\"id\":1334444,\"type\":\"subscribe\",\"topic\":\"/contractMarket/ticker:BASEQUOTEPERP\",\"privateChannel\":false,\"response\":true}";

        getAssets(true).forEach(base -> {
            Arrays.asList("USD", "USDT").forEach(quote -> {
                var msg = subscribeMsgTemplate.replace("_ID_", incAndGetIdAsString()).replace("BASE", base).replace("QUOTE", quote);
                System.out.println(msg);
                this.sendMessage(msg);
            });


        });
    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("PERP")) {
            return Optional.empty();
        }

        var futureResponse = objectMapper.readValue(message, FutureResponse.class);

        var pair = SymbolHelper.getPair(futureResponse.getData().getSymbol().replace("PERP", ""));


        return Optional.of(Collections.singletonList(Ticker.builder().exchange(getExchange()).source(Source.WS).timestamp(currentTimestamp()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(Double.parseDouble(futureResponse.getData().getPrice())).build()));
    }

    @Scheduled(every = "58s")
    public void ping() {
        this.sendMessage("{\"id\":\"_ID_\",\"type\":\"ping\"}".replace("_ID_", incAndGetIdAsString()));
    }


}
