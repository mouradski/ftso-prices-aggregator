package dev.mouradski.ftso.trades.client.binance;

import jakarta.websocket.ClientEndpoint;
import org.springframework.stereotype.Component;

@Component
@ClientEndpoint
public class BinanceUsClientEndpoint extends BinanceClientEndpoint {

    @Override
    protected String getWebsocketApiBase() {
        return "wss://stream.binance.us:9443/stream?streams=";
    }

    protected String getExchange() {
        return "binanceus";
    }
}
