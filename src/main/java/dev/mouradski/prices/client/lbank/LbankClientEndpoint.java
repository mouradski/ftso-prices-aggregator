package dev.mouradski.prices.client.lbank;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.prices.client.AbstractClientEndpoint;
import dev.mouradski.prices.model.Trade;
import dev.mouradski.prices.service.PriceService;
import dev.mouradski.prices.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ClientEndpoint
@Component
public class LbankClientEndpoint extends AbstractClientEndpoint {

    protected LbankClientEndpoint(PriceService priceSender, @Value("${exchanges}") List<String> exchanges, @Value("${assets}") List<String> assets) {
        super(priceSender, exchanges, assets);
    }

    @Override
    protected String getUri() {
        return "wss://www.lbkex.net/ws/V2/";
    }

    @Override
    protected void subscribe() {
        getAssets().stream().map(String::toUpperCase).forEach(symbol -> {
            Arrays.asList("USD", "USDT", "USDC").forEach(quote -> {
                this.sendMessage("{\"action\":\"subscribe\", \"subscribe\":\"trade\", \"pair\":\"SYMBOL_QUOTE\"}".replaceAll("SYMBOL", symbol).replace("QUOTE", quote));
            });
        });
    }

    @Override
    protected String getExchange() {
        return "lbank";
    }

    @Override
    protected boolean pong(String message) {
        if (message.contains("ping")) {
            this.sendMessage(message.replaceAll("ping", "pong"));
            return true;
        }

        return false;
    }

    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {

        if (!message.contains("\"trade\"")) {
            return new ArrayList<>();
        }

        var tradeWrapper = objectMapper.readValue(message, TradeWrapper.class);

        var symbol = SymbolHelper.getSymbol(tradeWrapper.getPair());

        return Arrays.asList(Trade.builder().exchange(getExchange()).symbol(symbol.getLeft()).quote(symbol.getRight()).price(tradeWrapper.getTrade().getPrice()).amount(tradeWrapper.getTrade().getAmount()).build());
    }

}
