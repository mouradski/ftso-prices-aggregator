package dev.mouradski.ftsopriceclient.client.fmfw;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftsopriceclient.client.AbstractClientEndpoint;
import dev.mouradski.ftsopriceclient.model.Trade;
import dev.mouradski.ftsopriceclient.service.PriceService;
import dev.mouradski.ftsopriceclient.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@ClientEndpoint
@Component
public class FmfwClientEndpoint extends AbstractClientEndpoint {

    protected FmfwClientEndpoint(PriceService priceSender, @Value("${exchanges}") List<String> exchanges, @Value("${assets}") List<String> assets) {
        super(priceSender, exchanges, assets);
    }

    @Override
    protected String getUri() {
        return "wss://api.fmfw.io/api/3/ws/public";
    }

    @Override
    protected void subscribe() {

        var pairs = new ArrayList<String>();

        getAssets().stream().map(String::toUpperCase).forEach(symbol -> {
            getAllQuotesExceptBusd(true).forEach(quote -> {
                pairs.add("\"" + symbol + quote + "\"");
            });
        });

        this.sendMessage("{\"method\":\"subscribe\", \"ch\":\"trades\", \"params\":{\"symbols\": [PAIRS]}, \"id\": ID}".replace("ID", counter.getCount().toString()).replace("PAIRS", pairs.stream().collect(Collectors.joining(","))));
    }

    @Override
    protected String getExchange() {
        return "fmfw";
    }

    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {
        if (!message.contains("\"p\"") || !message.contains("trade") || !message.contains("update")) {
            return new ArrayList<>();
        }

        var fmfwTradeResponse = objectMapper.readValue(message, FmfwTradeResponse.class);

        var trades = new ArrayList<Trade>();

        fmfwTradeResponse.getUpdate().getTrades().entrySet().forEach(e -> {
            var symbol = SymbolHelper.getSymbol(e.getKey());

            e.getValue().stream().sorted(Comparator.comparing(FmfwTradeResponse.Trade::getT)).forEach(trade -> {
                trades.add(Trade.builder().exchange(getExchange()).symbol(symbol.getLeft()).quote(symbol.getRight()).price(trade.getP()).amount(trade.getQ()).build());
            });
        });

        return trades;
    }
}
