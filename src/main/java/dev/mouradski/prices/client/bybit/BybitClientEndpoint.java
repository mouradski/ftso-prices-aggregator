package dev.mouradski.prices.client.bybit;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.prices.client.AbstractClientEndpoint;
import dev.mouradski.prices.model.Trade;
import dev.mouradski.prices.service.PriceService;
import dev.mouradski.prices.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@ClientEndpoint
public class BybitClientEndpoint extends AbstractClientEndpoint {
    protected BybitClientEndpoint(PriceService priceSender, @Value("${exchanges}") List<String> exchanges, @Value("${assets}") List<String> assets) {
        super(priceSender, exchanges, assets);
    }

    @Override
    protected String getUri() {
        return "wss://stream.bybit.com/spot/quote/ws/v2";
    }

    @Override
    protected void subscribe() {
        getAssets().stream().map(String::toUpperCase).forEach(symbol -> {
            getAllQuotesExceptBusd(true).forEach(quote -> {
                this.sendMessage("{\"topic\":\"trade\", \"params\":{\"symbol\":\"SYMBOLQUOTE\", \"binary\":false}, \"event\":\"sub\"}".replace("SYMBOL", symbol).replace("QUOTE", quote));

            });
        });
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

        var tradeResponse = gson.fromJson(message, TradeResponse.class);

        Pair<String, String> symbol = SymbolHelper.getSymbol(tradeResponse.getParams().getSymbol());

        return Arrays.asList(Trade.builder().exchange(getExchange()).symbol(symbol.getLeft()).quote(symbol.getRight()).price(Double.parseDouble(tradeResponse.getData().get("p"))).amount(Double.parseDouble(tradeResponse.getData().get("p"))).build());
    }
}
