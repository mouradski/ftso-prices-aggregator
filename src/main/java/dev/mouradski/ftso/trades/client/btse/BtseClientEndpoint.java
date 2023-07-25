package dev.mouradski.ftso.trades.client.btse;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ClientEndpoint
public class BtseClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://ws.btse.com/ws/spot";
    }

    @Override
    protected void subscribe() {
        var pairs = new ArrayList<String>();
        getAssets(true).stream()
                .filter(base -> !getAllQuotes(true).contains(base))
                .forEach(symbol -> getAllQuotesExceptBusd(true).forEach(quote -> {
                    var symbolId = symbol + "-" + quote;
                    pairs.add("\"tradeHistoryApi:SYMBOL\"".replace("SYMBOL", symbolId));
                    this.sendMessage("{\"op\": \"subscribe\",\"args\": [\"tradeHistoryApi:SYMBOL\"]}".replace("SYMBOL",
                            symbolId));
                }));
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
            var pair = SymbolHelper.getPair(tradeHistoryData.getSymbol());

            trades.add(Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight())
                    .price(tradeHistoryData.getPrice()).amount(tradeHistoryData.getSize())
                    .timestamp(currentTimestamp()).build());
        });

        return trades;
    }
}
