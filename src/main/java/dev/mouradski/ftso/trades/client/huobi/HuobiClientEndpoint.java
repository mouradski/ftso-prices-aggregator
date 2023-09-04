package dev.mouradski.ftso.trades.client.huobi;

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
public class HuobiClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://api.huobi.pro/ws";
    }


    @Override
    protected void subscribeTrade() {

        getAssets().forEach(symbol -> getAllQuotesExceptBusd(false).forEach(base -> this.sendMessage("{   \"sub\": \"market." + symbol + base + ".trade.detail\",   \"id\": \"ID\" }".replace("ID", new Date().getTime() + ""))));
    }

    @Override
    protected void subscribeTicker() {
        getAssets().forEach(symbol -> getAllQuotesExceptBusd(false).forEach(base -> this.sendMessage("{   \"sub\": \"market." + symbol + base + ".ticker\",   \"id\": \"ID\" }".replace("ID", new Date().getTime() + ""))));
    }

    @Override
    protected String getExchange() {
        return "huobi";
    }

    @Override
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {
        var tradeMessage = gson.fromJson(message, TradeResponse.class);

        if (tradeMessage.getCh() == null || !tradeMessage.getCh().contains("trade")) {
            return Optional.empty();
        }

        var trades = new ArrayList<Trade>();

        var symbolId = tradeMessage.getCh().split("\\.")[1].toUpperCase();

        var pair = SymbolHelper.getPair(symbolId);

        tradeMessage.getTick().getData().stream().sorted(Comparator.comparing(TradeDetail::getTradeId)).forEach(huobiTrade -> trades.add(Trade.builder().exchange(getExchange())
                .base(pair.getLeft()).quote(pair.getRight()).price(huobiTrade.getPrice())
                .amount(huobiTrade.getAmount()).timestamp(currentTimestamp()).build()));

        return Optional.of(trades);
    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("ticker") || !message.contains("open")) {
            return Optional.empty();
        }

        var tickerMessage = gson.fromJson(message, TickerResponse.class);


        var symbolId = tickerMessage.getCh().split("\\.")[1].toUpperCase();

        var par = SymbolHelper.getPair(symbolId);

        return Optional.of(Collections.singletonList(Ticker.builder().exchange(getExchange()).base(par.getLeft()).quote(par.getRight()).lastPrice(tickerMessage.getTick().getLastPrice()).timestamp(currentTimestamp()).build()));
    }

    @Override
    protected boolean pong(String message) {
        if (message.contains("ping")) {
            this.sendMessage(message.replace("ping", "pong"));
            return true;
        }

        return false;
    }
}
