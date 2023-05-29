package dev.mouradski.ftsopriceclient.client.binance;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftsopriceclient.utils.Constants;
import dev.mouradski.ftsopriceclient.client.AbstractClientEndpoint;
import dev.mouradski.ftsopriceclient.model.Trade;
import dev.mouradski.ftsopriceclient.service.PriceService;
import dev.mouradski.ftsopriceclient.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@ClientEndpoint
public class BinanceClientEndpoint extends AbstractClientEndpoint {

    public BinanceClientEndpoint(PriceService priceSender) {
        super(priceSender);
    }

    protected String getWebsocketApiBase() {
        return "wss://stream.binance.com:9443/stream?streams=";
    }


    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {
        var binanceTrade = objectMapper.readValue(message, BinanceTrade.class);

        Pair<String, String> symbol = SymbolHelper.getQuote(binanceTrade.getData().getS());

        return Arrays.asList(Trade.builder().exchange(getExchange()).symbol(symbol.getLeft()).quote(symbol.getRight())
                .price(binanceTrade.getData().getP()).amount(binanceTrade.getData().getQ()).build());
    }

    @Override
    protected String getUri() {
        var sb = new StringBuilder();

        Constants.SYMBOLS.forEach(symbol -> {
            getAllQuotes(false).stream()
                    .filter(quote -> !quote.equals(symbol)).forEach(quote -> {
                        if (!sb.toString().isEmpty()) {
                            sb.append("/");
                        }
                        sb.append(symbol).append(quote).append("@trade");
                    });
        });


        return getWebsocketApiBase() + sb;
    }


    @Override
    protected void subscribe() {

    }

    @Override
    protected String getExchange() {
        return "binance";
    }
}
