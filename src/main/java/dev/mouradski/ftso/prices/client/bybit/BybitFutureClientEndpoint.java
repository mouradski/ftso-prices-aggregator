package dev.mouradski.ftso.prices.client.bybit;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

@ApplicationScoped
@ClientEndpoint
@Startup
public class BybitFutureClientEndpoint extends BybitClientEndpoint {
    @Override
    protected String getUri() {
        return "wss://stream.bybit.com/v5/public/linear";
    }

    @Override
    protected String getExchange() {
        return "bybitfuture";
    }


}
