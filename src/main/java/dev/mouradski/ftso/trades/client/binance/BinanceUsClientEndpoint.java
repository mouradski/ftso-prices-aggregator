package dev.mouradski.ftso.trades.client.binance;

import dev.mouradski.ftso.trades.service.PriceService;
import jakarta.websocket.ClientEndpoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ClientEndpoint
public class BinanceUsClientEndpoint extends BinanceClientEndpoint {

    public BinanceUsClientEndpoint(PriceService priceSender, @Value("${exchanges}") List<String> exchanges, @Value("${assets}") List<String> assets) {
        super(priceSender, exchanges, assets);
    }

    protected String getWebsocketApiBase() {
        return "wss://stream.binance.us:9443/stream?streams=";
    }

    protected String getExchange() {
        return "binanceus";
    }
}
