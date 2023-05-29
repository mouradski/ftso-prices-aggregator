package dev.mouradski.ftsopriceclient.client.cex;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.mouradski.ftsopriceclient.utils.Constants;
import dev.mouradski.ftsopriceclient.client.AbstractClientEndpoint;
import dev.mouradski.ftsopriceclient.model.Trade;
import dev.mouradski.ftsopriceclient.service.PriceService;
import jakarta.websocket.ClientEndpoint;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@ClientEndpoint
public class CexClientEndpoint extends AbstractClientEndpoint {

    private ObjectMapper objectMapper = new ObjectMapper();

    protected CexClientEndpoint(PriceService priceSender) {
        super(priceSender);
    }

    @Override
    protected String getUri() {
        return "wss://ws.cex.io/ws";
    }

    @Override
    protected void subscribe() {

        this.sendMessage("{\"e\":\"subscribe\", \"rooms\":[\"tickers\"]}");
    }

    @Override
    protected String getExchange() {
        return "cex";
    }

    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {
        var tick = objectMapper.readValue(message, Tick.class);

        if (Constants.SYMBOLS.contains(tick.getData().getSymbol1().toLowerCase()) && Constants.USD_USDT_USDC_BUSD.contains(tick.getData().getSymbol2().toLowerCase())) {
            return Arrays.asList(Trade.builder().exchange(getExchange()).symbol(tick.getData().getSymbol1()).quote(tick.getData().getSymbol2()).price(tick.getData().getPrice()).amount(tick.getData().getVolume()).build());
        }

        return new ArrayList<>();

    }

    @Override
    protected void pong(String message) {
        if (message.contains("ping")) {
           this.sendMessage("{\"e\":\"pong\"}");
        }
    }

    @Override
    protected long getTimeout() {
        return 600;
    }
}
