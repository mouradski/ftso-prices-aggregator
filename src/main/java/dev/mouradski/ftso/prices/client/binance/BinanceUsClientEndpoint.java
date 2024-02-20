package dev.mouradski.ftso.prices.client.binance;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

@ApplicationScoped
@ClientEndpoint
@Startup
public class BinanceUsClientEndpoint extends BinanceClientEndpoint {

    @Override
    protected String getWebsocketApiBase() {
        return "wss://stream.binance.us:9443/stream?streams=";
    }

    protected String getExchange() {
        return "binanceus";
    }
}
