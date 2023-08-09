package dev.mouradski.ftso.trades.client.gemini;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@ClientEndpoint
public class GeminiClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://api.gemini.com/v1/multimarketdata?symbols=" +  getMarkets() + "&trades=true&top_of_book=true";
    }

    private String getMarkets() {
        return getAssets(true).stream().map(v -> v + "USDT").collect(Collectors.joining(","));
    }

    @Override
    protected String getExchange() {
        return "gemini";
    }

    @Override
    protected void subscribe() {
    }

    @Override
    protected boolean pong(String message) {
        return super.pong(message);
    }

    @Override
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {

        if (!message.contains("trade")) {
            return Optional.empty();
        }

        var evenWrapper = this.objectMapper.readValue(message, GeminiTrade.class);

        var trades = new ArrayList<Trade>();


        evenWrapper.getEvents().stream().sorted(Comparator.comparing(GeminiTrade.Event::getTradeId)).filter(event -> "trade".equals(event.getEventType())).forEach(event -> {
            var symbol = SymbolHelper.getPair(event.getSymbol());
            trades.add(Trade.builder().base(symbol.getLeft()).quote(symbol.getRight()).exchange(getExchange()).timestamp(currentTimestamp()).price(event.getPrice()).amount(event.getAmount()).timestamp(currentTimestamp()).build());
        });

        return Optional.of(trades);
    }
}
