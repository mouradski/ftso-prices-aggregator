package dev.mouradski.ftso.trades.server;

import dev.mouradski.ftso.trades.model.Ticker;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/ticker")
@Slf4j
@ApplicationScoped
public class TickerServer extends WsServer<Ticker> {

    protected static final Set<WsServer<Ticker>> listeners = new CopyOnWriteArraySet<>();

    @Override
    protected Set<WsServer<Ticker>> getListeners() {
        return listeners;
    }
}
