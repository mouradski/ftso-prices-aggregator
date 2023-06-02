package dev.mouradski.prices.client.huobi;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.prices.client.AbstractClientEndpoint;
import dev.mouradski.prices.model.Trade;
import dev.mouradski.prices.service.PriceService;
import dev.mouradski.prices.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@ClientEndpoint
@Component
public class HuobiClientEndpoint extends AbstractClientEndpoint {
    protected HuobiClientEndpoint(PriceService priceSender, @Value("${exchanges}") List<String> exchanges, @Value("${assets}") List<String> assets) {
        super(priceSender, exchanges, assets);
    }

    @Override
    protected String getUri() {
        return "wss://api.huobi.pro/ws";
    }


    @Override
    protected void subscribe() {


        getAssets().forEach(symbol -> {
            getAllQuotesExceptBusd(false).forEach(quote -> {
                this.sendMessage("{   \"sub\": \"market." + symbol + quote + ".trade.detail\",   \"id\": \"ID\" }".replace("ID", new Date().getTime() + ""));

            });
        });

    }

    @Override
    protected String getExchange() {
        return "huobi";
    }

    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {

        var trades = new ArrayList<Trade>();

        var tradeMessage = gson.fromJson(message, Response.class);

        if (tradeMessage.getCh() == null) {
            return new ArrayList<>();
        }

        var pair = tradeMessage.getCh().split("\\.")[1].toUpperCase();

        var symbol = SymbolHelper.getSymbol(pair);

        tradeMessage.getTick().getData().stream().sorted(Comparator.comparing(TradeDetail::getTradeId)).forEach(huobiTrade -> {
            trades.add(Trade.builder().exchange(getExchange())
                    .symbol(symbol.getLeft()).quote(symbol.getRight()).price(huobiTrade.getPrice())
                    .amount(huobiTrade.getAmount()).build());
        });

        return trades;
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
