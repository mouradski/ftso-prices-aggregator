package dev.mouradski.ftso.trades.client.btcex;

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
import java.util.List;

@Component
@ClientEndpoint
public class BtcexClientEndpoint extends AbstractClientEndpoint {

    protected BtcexClientEndpoint(TradeService priceSender, @Value("${exchanges}") List<String> exchanges,
            @Value("${assets}") List<String> assets) {
        super(priceSender, exchanges, assets);
    }

    @Override
    protected String getUri() {
        return "wss://api.btcex.com/ws/api/v1";
    }

    @Override
    protected void subscribe() {
        getAssets(true).stream().filter(base -> !getAllQuotes(true).contains(base)).forEach(symbol -> {
            var msg = "{\"jsonrpc\" : \"2.0\",\"id\" : ID,\"method\" : \"/public/subscribe\",\"params\" : {\"channels\":[\"trades.SYMBOL-USDT-SPOT.raw\"]}}"
                    .replace("ID", incAndGetIdAsString()).replace("SYMBOL", symbol);
            this.sendMessage(msg);
        });
    }

    @Override
    protected String getExchange() {
        return "btcex";
    }

    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {
        if (!message.contains("trade_id")) {
            return new ArrayList<>();
        }

        var tradeResponse = objectMapper.readValue(message, TradeResponse.class);

        var trades = new ArrayList<Trade>();

        tradeResponse.getParams().getData().forEach(tradeData -> {
            var pair = SymbolHelper.getPair(tradeData.getInstrumentName().replace("-SPOT", ""));

            trades.add(Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight())
                    .price(tradeData.getPrice()).amount(tradeData.getAmount())
                    .timestamp(Long.parseLong(tradeData.getTimestamp()) * 1000) // timestamp is sent in seconds
                    .build());
        });

        return trades;
    }

    @Scheduled(fixedDelay = 15000)
    public void ping() {
        this.sendMessage("{ \"jsonrpc\":\"2.0\",\"id\": ID,\"method\": \"/public/ping\",\"params\":{}}".replace("ID",
                incAndGetIdAsString()));
    }
}
