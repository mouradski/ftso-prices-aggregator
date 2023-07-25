package dev.mouradski.ftso.trades.client.bybit;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@ClientEndpoint
public class BybitClientEndpoint extends AbstractClientEndpoint {
    @Override
    protected String getUri() {
        return "wss://stream.bybit.com/spot/quote/ws/v2";
    }

    @Override
    protected void subscribe() {
        getAssets().stream().map(String::toUpperCase)
                .forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> this.sendMessage(
                        "{\"topic\":\"trade\", \"params\":{\"symbol\":\"SYMBOLQUOTE\", \"binary\":false}, \"event\":\"sub\"}"
                                .replace("SYMBOL", base).replace("QUOTE", quote))));
    }

    @Override
    protected String getExchange() {
        return "bybit";
    }

    @Scheduled(fixedDelay = 30 * 1000)
    public void ping() {
        this.sendMessage("{\"ping\":" + new Date().getTime() + "}");
    }

    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {

        if (!message.contains("trade") || !message.contains("data")) {
            return new ArrayList<>();
        }

        var bybitTrade = objectMapper.readValue(message, BybitTrade.class);

        Pair<String, String> pair = SymbolHelper.getPair(bybitTrade.getParams().getSymbol());

        return Arrays.asList(Trade.builder().timestamp(currentTimestamp()).exchange(getExchange())
                .base(pair.getLeft()).quote(pair.getRight())
                .price(Double.parseDouble(bybitTrade.getData().getP()))
                .amount(Double.parseDouble(bybitTrade.getData().getQ())).build());
    }
}
