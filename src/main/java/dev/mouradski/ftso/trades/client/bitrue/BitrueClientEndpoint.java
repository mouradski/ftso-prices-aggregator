package dev.mouradski.ftso.trades.client.bitrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Ticker;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.util.*;

@ApplicationScoped
@ClientEndpoint
@Startup
public class BitrueClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://ws.bitrue.com/kline-api/ws";
    }

    @Override
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {

        var tradeMessage = gson.fromJson(message, TradeMessage.class);

        if (tradeMessage.getChannel() == null || tradeMessage.getTick() == null
                || tradeMessage.getTick().getData() == null) {
            return Optional.empty();
        }

        var pair = SymbolHelper.getPair(tradeMessage.getChannel().replace("market_", "").replace("_trade_ticker", ""));
        var base = pair.getLeft();
        var quote = pair.getRight();
        var trades = new ArrayList<Trade>();

        tradeMessage.getTick().getData().stream().sorted(Comparator.comparing(BitrueTrade::getTs))
                .forEach(trade -> trades.add(Trade.builder().exchange(getExchange()).price(trade.getPrice())
                        .amount(trade.getVol()).quote(quote).base(base).timestamp(currentTimestamp()).build()));

        return Optional.of(trades);
    }

    /*
     * private String[] parseSymbol(String channel) {
     * var parts = channel.toUpperCase().split("_");
     * 
     * if (parts.length < 2) {
     * throw new IllegalArgumentException("Invalid channel format");
     * }
     * 
     * return parts;
     * }
     */

    @Override
    protected void subscribeTrade() {
        getAssets().stream().forEach(base -> getAllStablecoinQuotesExceptBusd(false).forEach(quote -> this.sendMessage(
                "{\"event\":\"sub\",\"params\":{\"cb_id\":\"CB_ID\",\"channel\":\"market_CB_ID_trade_ticker\"}}"
                        .replaceAll("CB_ID", base + quote))));
    }

    @Override
    protected void subscribeTicker() {
        getAssets().stream().forEach(base -> getAllStablecoinQuotesExceptBusd(false).forEach(quote -> this.sendMessage(
                "{\"event\":\"sub\",\"params\":{\"cb_id\":\"CB_ID\",\"channel\":\"market_CB_ID_ticker\"}}"
                        .replaceAll("CB_ID", base + quote))));
    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("close")) {
            return Optional.empty();
        }

        var tickerResponse = objectMapper.readValue(message, TickerResponse.class);

        var pair = SymbolHelper
                .getPair(tickerResponse.getChannel().replace("market_", "").replace("_trade_ticker", ""));

        return Optional.of(Collections
                .singletonList(Ticker.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight())
                        .lastPrice(tickerResponse.getTick().getClose()).timestamp(currentTimestamp()).build()));
    }

    @Override
    protected String getExchange() {
        return "bitrue";
    }

    @Override
    protected long getTimeout() {
        return 30;
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
