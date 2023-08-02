package dev.mouradski.ftso.trades.server;


import com.fasterxml.jackson.databind.ObjectMapper;
import dev.mouradski.ftso.trades.model.Trade;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/trade")
@Slf4j
@ApplicationScoped
public class TradeServer {

    protected static final Set<TradeServer> listeners = new CopyOnWriteArraySet<>();
    private Session session;
    private ObjectMapper objectMapper = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        listeners.add(this);
    }

    @OnClose
    public void onClose(Session session) {
        listeners.remove(this);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
    }

    public void broadcastTrade(Trade trade) {
        try {
            System.out.println(trade);
            var messageAsString = objectMapper.writeValueAsString(trade);
            listeners.forEach(listener -> {
                System.out.println(trade);
                listener.sendMessage(messageAsString);
            });
        } catch (IOException e) {
            log.error("Caught exception while broadcasting trade : {}", trade, e);
        }
    }

    private void sendMessage(String message) {
        try {
            this.session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            log.error("Caught exception while sending message to Session " + this.session.getId(), e.getMessage(), e);
        }
    }
}
