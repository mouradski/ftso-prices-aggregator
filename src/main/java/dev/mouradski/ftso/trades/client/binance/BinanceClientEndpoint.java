package dev.mouradski.ftso.trades.client.binance;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.service.TradeService;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ClientEndpoint
public class BinanceClientEndpoint extends AbstractClientEndpoint {

    private final String websocketApiBase = "wss://stream.binance.com:9443/stream?streams=";

    public BinanceClientEndpoint(TradeService priceSender, @Value("${exchanges}") List<String> exchanges, @Value("${assets}") List<String> assets) {
        super(priceSender, exchanges, assets);
    }

    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {
        var binanceTrade = objectMapper.readValue(message, BinanceTrade.class);

        Pair<String, String> pair = SymbolHelper.getPair(binanceTrade.getData().getS());

        return Arrays.asList(Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight())
                .price(binanceTrade.getData().getP()).amount(binanceTrade.getData().getQ()).build());
    }

    @Override
    protected String getUri() {
        return getWebsocketApiBase() + getPairs();
    }

    private String getPairs() {
        return getAssets().stream()
                .flatMap(asset -> getAllQuotes(false).stream()
                        .filter(quote -> !quote.equals(asset))
                        .map(quote -> asset + quote + "@trade"))
                .collect(Collectors.joining("/"));
    }

    @Override
    protected void subscribe() {
    }

    @Override
    protected String getExchange() {
        return "binance";
    }

    protected String getWebsocketApiBase() {
        return websocketApiBase;
    }
}
