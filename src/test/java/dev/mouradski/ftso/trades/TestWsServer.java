package dev.mouradski.ftso.trades;

import dev.mouradski.ftso.trades.server.WsServer;
import jakarta.websocket.server.ServerEndpoint;

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
