package dev.mouradski.ftsopriceclient.client.okex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.mouradski.ftsopriceclient.utils.Constants;
import dev.mouradski.ftsopriceclient.client.AbstractClientEndpoint;
import dev.mouradski.ftsopriceclient.model.Trade;
import dev.mouradski.ftsopriceclient.service.PriceService;
import dev.mouradski.ftsopriceclient.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ClientEndpoint
@Component
public class OkexClientEndpoint extends AbstractClientEndpoint {

    private ObjectMapper objectMapper = new ObjectMapper();

    protected OkexClientEndpoint(PriceService priceSender) {
        super(priceSender);
    }

    @Override
    protected String getUri() {
        return "wss://ws.okx.com:8443/ws/v5/public";
    }

    @Override
    protected void subscribe() {

        var subscriptionMsg = new StringBuilder("{\"op\": \"subscribe\",\"args\": [");

        var channels = new ArrayList<String>();

        Constants.SYMBOLS.stream().map(String::toUpperCase).forEach(symbol -> {
            Arrays.asList("USD", "USDT", "USDC").forEach(quote -> {
                channels.add("{\"channel\": \"trades\",\"instId\": \"SYMBOL-QUOTE\"}".replace("SYMBOL", symbol).replace("QUOTE", quote));
            });
        });

        subscriptionMsg.append(channels.stream().collect(Collectors.joining(",")));
        subscriptionMsg.append("]}");

        this.sendMessage(subscriptionMsg.toString());

    }

    @Override
    protected String getExchange() {
        return "okex";
    }

    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {
        var tradeData = objectMapper.readValue(message, TradeData.class);

        var symbol = SymbolHelper.getQuote(tradeData.getArg().getInstId());

        var trades = new ArrayList<Trade>();

        tradeData.getData().forEach(data -> {
            trades.add(Trade.builder().exchange(getExchange()).symbol(symbol.getLeft()).quote(symbol.getRight()).price(data.getPx()).amount(data.getSz()).build());
        });

        return trades;
    }
}
