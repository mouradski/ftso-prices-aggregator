package dev.mouradski.ftso.trades.client.btse;


import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.service.PriceService;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ClientEndpoint
public class BtseClientEndpoint extends AbstractClientEndpoint {

    protected BtseClientEndpoint(PriceService priceSender, @Value("${exchanges}") List<String> exchanges, @Value("${assets}") List<String> assets) {
        super(priceSender, exchanges, assets);
    }

    @Override
    protected String getUri() {
        return "wss://ws.btse.com/ws/spot";
    }

    @Override
    protected void subscribe() {
        var pairs = new ArrayList<String>();
        getAssets(true).stream()
                .filter(symbol -> !getAllQuotes(true).contains(symbol))
                .forEach(symbol -> {
                    getAllQuotesExceptBusd(true).forEach(quote -> {
                        var symbolId = symbol + "-" + quote;
                        pairs.add("\"tradeHistoryApi:SYMBOL\"".replace("SYMBOL", symbolId));
                        this.sendMessage("{\"op\": \"subscribe\",\"args\": [\"tradeHistoryApi:SYMBOL\"]}".replace("SYMBOL", symbolId));
                    });
                });
    }

    @Override
    protected String getExchange() {
        return "btse";
    }

    @Override
    protected boolean pong(String message) {
        if (message.contains("ping")) {
            this.sendMessage("pong");
            return true;
        }

        return false;
    }

    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {

        if (!message.contains("tradeId")) {
            return new ArrayList<>();
        }

        var tradeHistoryResponse = this.objectMapper.readValue(message, TradeHistoryResponse.class);

        var trades = new ArrayList<Trade>();

        tradeHistoryResponse.getData().forEach(tradeHistoryData -> {
            var symbol = SymbolHelper.getSymbol(tradeHistoryData.getSymbol());

            trades.add(Trade.builder().exchange(getExchange()).symbol(symbol.getLeft()).quote(symbol.getRight())
                    .price(tradeHistoryData.getPrice()).amount(tradeHistoryData.getSize()).build());
        });

        return trades;
    }
}
