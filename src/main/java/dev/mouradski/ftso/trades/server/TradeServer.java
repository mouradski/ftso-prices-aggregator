package dev.mouradski.ftso.trades.server;


import com.fasterxml.jackson.databind.ObjectMapper;
import dev.mouradski.ftso.trades.model.Ticker;
import dev.mouradski.ftso.trades.model.Trade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;

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
            var messageAsString = objectMapper.writeValueAsString(trade);
            listeners.forEach(listener -> {
                listener.sendMessage(messageAsString);
            });
        } catch (IOException e) {
            log.error("Caught exception while broadcasting trade : {}", trade, e);
        }
    }


    public void broadcastTicker(Ticker ticker) {
        try {
            var messageAsString = objectMapper.writeValueAsString(ticker);
            listeners.forEach(listener -> {
                listener.sendMessage(messageAsString);
            });
        } catch (IOException e) {
            log.error("Caught exception while broadcasting ticker : {}", ticker, e);
        }
    }

    private void sendMessage(String message) {
        try {
            this.session.getAsyncRemote().sendText(message);
        } catch (Exception e) {
            log.error("Caught exception while sending message to Session " + this.session.getId(), e.getMessage(), e);
        }
    }
}
