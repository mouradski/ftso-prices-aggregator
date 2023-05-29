package dev.mouradski.ftsopriceclient.client.binance;

import dev.mouradski.ftsopriceclient.service.PriceService;
import jakarta.websocket.ClientEndpoint;
import org.springframework.stereotype.Component;

@Component
@ClientEndpoint
public class BinanceUsClientEndpoint extends BinanceClientEndpoint {

    public BinanceUsClientEndpoint(PriceService priceSender) {
        super(priceSender);
    }

    protected String getWebsocketApiBase() {
        return "wss://stream.binance.us:9443/stream?streams=";
    }

    protected String getExchange() {
        return "binanceus";
    }
}
