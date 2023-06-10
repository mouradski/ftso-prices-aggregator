package dev.mouradski.ftso.trades.client.bitget;

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

@ClientEndpoint
@Component
public class BitgetClientEndpoint extends AbstractClientEndpoint {

    protected BitgetClientEndpoint(TradeService priceSender, @Value("${exchanges}") List<String> exchanges, @Value("${assets}") List<String> assets) {
        super(priceSender, exchanges, assets);
    }

    @Override
    protected String getUri() {
        return "wss://ws.bitget.com/spot/v1/stream";
    }

    @Override
    protected void subscribe() {
        getAssets(true).forEach(base -> {
            getAllQuotesExceptBusd(true).forEach(quote -> {
                this.sendMessage("{\"op\": \"subscribe\",\"args\": [{\"instType\": \"sp\",\"channel\": \"trade\",\"instId\": \"PAIR\"}]}".replace("PAIR", base + quote));
            });
        });
    }

    @Override
    protected String getExchange() {
        return "bitget";
    }


    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {

        if (message.contains("error") || !message.contains("update")) {
            return new ArrayList<>();
        }

        var trades = new ArrayList<Trade>();

        var update = this.objectMapper.readValue(message, Update.class);

        var pair = SymbolHelper.getPair(update.getArg().getInstId());


        update.getData().forEach(tradeData -> {
            trades.add(Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).amount(tradeData.getQuantity()).price(tradeData.getPrice()).build());
        });

        return trades;
    }

    @Scheduled(fixedDelay = 30000)
    public void ping() {
        this.sendMessage("ping");
    }
}
