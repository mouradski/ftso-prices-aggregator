package dev.mouradski.ftso.prices.client.bequant;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
@ClientEndpoint
@Startup
public class BequantClientEndpoint extends AbstractClientEndpoint {
    @Override
    protected String getUri() {
        return "wss://api.bequant.io/api/3/ws/public";
    }

    @Override
    protected String getExchange() {
        return "bequant";
    }

    @Override
    protected void subscribeTicker() {
        var bases = getAssets(true).stream().map(v -> "\"" + v + "\"").collect(Collectors.joining(","));

        getAllQuotes(true).forEach(quote -> sendMessage("{\"method\": \"subscribe\",\"ch\": \"price/rate/1s\",\"params\": {\"currencies\": [BASES],\"target_currency\": \"QUOTE\"},\"id\": ID}"
                .replace("ID", incAndGetIdAsString())
                .replace("QUOTE", quote).replace("BASES", bases)));
    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("price/rate/1s")) {
            return Optional.empty();
        }

        var cryptoRate = objectMapper.readValue(message, CryptoRate.class);
        var quote = cryptoRate.getTargetCurrency();

        var tickers = new ArrayList<Ticker>();

        cryptoRate.getData().entrySet().forEach(e -> {
            tickers.add(Ticker.builder().source(Source.WS).exchange(getExchange()).base(e.getKey()).quote(quote).lastPrice(Double.parseDouble(e.getValue().getR())).timestamp(currentTimestamp()).build());
        });

        return Optional.of(tickers);
    }
}
