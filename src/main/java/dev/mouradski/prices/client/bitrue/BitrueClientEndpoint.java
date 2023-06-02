package dev.mouradski.prices.client.bitrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.prices.client.AbstractClientEndpoint;
import dev.mouradski.prices.model.Trade;
import dev.mouradski.prices.service.PriceService;
import jakarta.websocket.ClientEndpoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static dev.mouradski.prices.utils.Constants.USDT;

@ClientEndpoint
@Component
public class BitrueClientEndpoint extends AbstractClientEndpoint {

    public BitrueClientEndpoint(PriceService priceSender, @Value("${exchanges}") List<String> exchanges, @Value("${assets}") List<String> assets) {
        super(priceSender, exchanges, assets);
    }

    @Override
    protected String getUri() {
        return "wss://ws.bitrue.com/kline-api/ws";
    }

    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {
        var trades = new ArrayList<Trade>();

        var tradeMessage = gson.fromJson(message, TradeMessage.class);

        if (tradeMessage.getChannel() == null || tradeMessage.getTick() == null || tradeMessage.getTick().getData() == null) {
            return trades;
        }

        var symbol = parseSymbol(tradeMessage.getChannel());
        var quote = "USDT";

        for (var trade : tradeMessage.getTick().getData()) {
            trades.add(Trade.builder().exchange(getExchange()).price(trade.getPrice()).amount(trade.getAmount()).quote(quote).symbol(symbol).build());
        }

        return trades;
    }

    private String parseSymbol(String channel) {
        var parts = channel.split("_");

        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid channel format");
        }

        return parts[1].replace("usdt", "").toUpperCase();
    }

    @Override
    protected void subscribe() {
        getAssets().stream().filter(v -> !v.contains("usd")).forEach(symbol -> {
            this.sendMessage("{\"event\":\"sub\",\"params\":{\"cb_id\":\"CB_ID\",\"channel\":\"market_CB_ID_trade_ticker\"}}".replaceAll("CB_ID", symbol + USDT));
        });
    }

    @Override
    protected String getExchange() {
        return "bitrue";
    }


    @Override
    protected long getTimeout() {
        return 300;
    }

    @Override
    protected boolean pong(String message) {

        if (message.contains("ping")) {
            this.sendMessage(message.replaceAll("ping", "pong"));
            return true;
        }

        return false;
    }
}
