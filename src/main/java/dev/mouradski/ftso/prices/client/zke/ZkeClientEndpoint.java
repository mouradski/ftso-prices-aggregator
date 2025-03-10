package dev.mouradski.ftso.prices.client.zke;

import dev.mouradski.ftso.prices.client.koinbay.KoinbayClientEndpoint;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

@ApplicationScoped
@ClientEndpoint
@Startup
public class ZkeClientEndpoint extends KoinbayClientEndpoint {
    @Override
    protected String getUri() {
        return "wss://ws.zke.com/kline-api/ws";
    }

    @Override
    protected String getExchange() {
        return "zke";
    }
}
