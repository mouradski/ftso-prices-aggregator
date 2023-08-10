package dev.mouradski.ftso.trades.client.upbit;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@ClientEndpoint
public class UpbitClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://api.upbit.com/websocket/v1";
    }

    @Override
    protected void subscribe() {

        var pairs = new ArrayList<String>();

        getAssets(true).forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> pairs.add("\"" + quote + "-" + base + "\"")));
        this.sendMessage("[{\"ticket\":\"trades\"},{\"type\":\"trade\",\"codes\":[SYMBOL]}]".replace("SYMBOL", String.join(",", pairs)));


    }

    @Override
    protected String getExchange() {
        return "upbit";
    }

    @Scheduled(every="100s")
    public void ping() {
        this.sendMessage("PING");
    }

    @Override
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {
        if (!message.contains("trade_price")) {
            return Optional.empty();
        }

        var trade = objectMapper.readValue(message, UpbitTrade.class);

        var base = trade.getCode().split("-")[1];
        var quote = trade.getCode().split("-")[0];

        return Optional.of(Collections.singletonList(Trade.builder().exchange(getExchange()).base(base).quote(quote).price(trade.getTradePrice()).amount(trade.getTradeVolume()).timestamp(currentTimestamp()).build()));
    }
}
