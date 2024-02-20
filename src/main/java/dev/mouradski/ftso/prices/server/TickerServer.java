package dev.mouradski.ftso.prices.server;

import dev.mouradski.ftso.prices.model.Ticker;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/ticker")
@Slf4j
@ApplicationScoped
@Startup
public class TickerServer extends WsServer<Ticker> {

    protected static final Set<Session> sessions = new CopyOnWriteArraySet<>();

    @Override
    protected Set<Session> getSessions() {
        return sessions;
    }
}
