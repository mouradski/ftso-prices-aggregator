package dev.mouradski.ftso.trades.client.upbit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.service.TradeService;
import jakarta.websocket.ClientEndpoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ClientEndpoint
@Component
public class UpbitClientEndpoint extends AbstractClientEndpoint {

    private ObjectMapper objectMapper = new ObjectMapper();

    protected UpbitClientEndpoint(TradeService priceSender, @Value("${exchanges}") List<String> exchanges, @Value("${assets}") List<String> assets) {
        super(priceSender, exchanges, assets);
    }

    @Override
    protected String getUri() {
        return "wss://api.upbit.com/websocket/v1";
    }

    @Override
    protected void subscribe() {

        var pairs = new ArrayList<String>();

        getAssets(true).forEach(base -> {
            getAllQuotesExceptBusd(true).forEach(quote -> {
                pairs.add("\"" + quote + "-" + base + "\"");

            });
        });
        this.sendMessage("[{\"ticket\":\"trades\"},{\"type\":\"trade\",\"codes\":[SYMBOL]}]".replace("SYMBOL", pairs.stream().collect(Collectors.joining(","))));


    }

    @Override
    protected String getExchange() {
        return "upbit";
    }

    @Scheduled(fixedDelay = 100 * 1000)
    public void ping() {
        this.sendMessage("PING");
    }

    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {
        if (!message.contains("trade_price")) {
            return new ArrayList<>();
        }

        var trade = objectMapper.readValue(message, UpbitTrade.class);

        var base = trade.getCode().split("-")[1];
        var quote = trade.getCode().split("-")[0];

        return Arrays.asList(Trade.builder().exchange(getExchange()).base(base).quote(quote).price(trade.getTradePrice()).amount(trade.getTradeVolume()).build());
    }
}
