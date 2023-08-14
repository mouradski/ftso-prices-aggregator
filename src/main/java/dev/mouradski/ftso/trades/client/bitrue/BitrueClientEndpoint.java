package dev.mouradski.ftso.trades.client.bitrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static dev.mouradski.ftso.trades.utils.Constants.USDT;

@ApplicationScoped
@ClientEndpoint

public class BitrueClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://ws.bitrue.com/kline-api/ws";
    }

    @Override
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {

        var tradeMessage = gson.fromJson(message, TradeMessage.class);

        if (tradeMessage.getChannel() == null || tradeMessage.getTick() == null || tradeMessage.getTick().getData() == null) {
            return Optional.empty();
        }

        var trades = new ArrayList<Trade>();


        var pair = parseSymbol(tradeMessage.getChannel());
        var quote = "USDT";

        tradeMessage.getTick().getData().stream().sorted(Comparator.comparing(BitrueTrade::getTs)).forEach(trade ->
                trades.add(Trade.builder().exchange(getExchange()).price(trade.getPrice()).amount(trade.getVol()).quote(quote).base(pair).timestamp(currentTimestamp()).build()));

        return Optional.of(trades);
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
        getAssets().stream().filter(v -> !v.contains("usd")).forEach(base -> this.sendMessage("{\"event\":\"sub\",\"params\":{\"cb_id\":\"CB_ID\",\"channel\":\"market_CB_ID_trade_ticker\"}}".replaceAll("CB_ID", base + USDT)));
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
