package dev.mouradski.ftso.trades;

import dev.mouradski.ftso.trades.server.WsServer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.server.ServerEndpoint;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;


@ServerEndpoint("/test")
//@ApplicationScoped
public class TestWsServer extends WsServer<Object> {

    public TestWsServer() {
        System.out.println("");
    }

    protected static final Set<WsServer<Object>> listeners = new CopyOnWriteArraySet<>();

    @Override
    protected Set<WsServer<Object>> getListeners() {
        return listeners;
    }
}
