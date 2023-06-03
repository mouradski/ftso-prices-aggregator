package dev.mouradski.ftso.trades.client.hitbtc;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.service.PriceService;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@ClientEndpoint
@Component
public class HitbtcClientEndpoint extends AbstractClientEndpoint {

    protected HitbtcClientEndpoint(PriceService priceSender, @Value("${exchanges}") List<String> exchanges, @Value("${assets}") List<String> assets) {
        super(priceSender, exchanges, assets);
    }

    @Override
    protected String getUri() {
        return "wss://api.hitbtc.com/api/3/ws/public";
    }


    @Override
    protected void subscribe() {
        var pairs = new ArrayList<String>();

        getAssets().stream().map(String::toUpperCase).forEach(symbol -> {
            getAllQuotesExceptBusd(true).forEach(quote -> {
                pairs.add("\"" + symbol + quote + "\"");
            });

        });

        this.sendMessage("{\"method\": \"subscribe\",\"ch\": \"trades\",\"params\": {\"symbols\": [PAIRS],\"limit\": 1},\"id\": ID}".replace("PAIRS", pairs.stream().collect(Collectors.joining(","))).replace("ID", new Date().getTime() + ""));
    }

    @Override
    protected String getExchange() {
        return "hitbtc";
    }

    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {
        var trades = new ArrayList<Trade>();

        var response = gson.fromJson(message, Response.class);

        if (response.getSnapshot() == null) {
            return new ArrayList<>();
        }

        response.getSnapshot().entrySet().forEach(e -> {
            var symbol = SymbolHelper.getSymbol(e.getKey());

            for (HitbtcTrade hitbtcTrade : e.getValue()) {
                trades.add(Trade.builder().exchange(getExchange()).symbol(symbol.getLeft()).quote(symbol.getRight()).price(hitbtcTrade.getP()).amount(hitbtcTrade.getQ()).build());
            }
        });

        response.getUpdate().entrySet().forEach(e -> {
            var symbol = SymbolHelper.getSymbol(e.getKey());


            for (HitbtcTrade hitbtcTrade : e.getValue()) {
                trades.add(Trade.builder().exchange(getExchange()).symbol(symbol.getLeft()).quote(symbol.getRight()).price(hitbtcTrade.getP()).amount(hitbtcTrade.getQ()).build());
            }
        });

        return trades;
    }

    @Override
    protected boolean pong(String message) {
        if (message.contains("ping")) {

            return true;
        }
        return false;
    }
}
