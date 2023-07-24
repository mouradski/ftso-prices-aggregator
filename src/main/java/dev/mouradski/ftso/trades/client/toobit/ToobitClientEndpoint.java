package dev.mouradski.ftso.trades.client.toobit;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.service.TradeService;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@ClientEndpoint
@Component
public class ToobitClientEndpoint extends AbstractClientEndpoint {

    protected ToobitClientEndpoint(TradeService priceSender, @Value("${exchanges}") List<String> exchanges, @Value("${assets}") List<String> assets) {
        super(priceSender, exchanges, assets);
    }

    @Override
    protected String getUri() {
        return "wss://stream.toobit.com/quote/ws/v1";
    }

    @Override
    protected void subscribe() {

        var pairs = getAssets(true).stream().map(v -> v + "USDT").collect(Collectors.joining(","));
        this.sendMessage("{\"symbol\": \"PAIRS\",\"topic\": \"trade\",\"event\": \"sub\",\"params\":{\"binary\": false}}".replace("PAIRS", pairs));
    }

    @Override
    protected String getExchange() {
        return "toobit";
    }

    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {

        if (!message.contains("symbolName")) {
            return new ArrayList<>();
        }

        var tradeMessage = this.objectMapper.readValue(message, TradeMessage.class);

        var pair = SymbolHelper.getPair(tradeMessage.getSymbolName());

        ArrayList<Trade> trades = tradeMessage.getTrades().stream().sorted(Comparator.comparing(ToobitTrade::getTime)).map(toobitTrade -> Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight())
                .price(toobitTrade.getPrice()).amount(toobitTrade.getQuantity()).timestamp(currentTimestamp()).build()).collect(Collectors.toCollection(ArrayList::new));

        return trades;

    }

    @Scheduled(fixedDelay = 4 * 60 * 1000)
    public void ping() {
        this.sendMessage("{\"ping\": ID}".replace("ID", new Date().getTime() + ""));
    }

}
