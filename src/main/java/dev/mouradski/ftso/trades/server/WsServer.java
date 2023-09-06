package dev.mouradski.ftso.trades.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Set;

@Slf4j
public abstract class WsServer<T> {
    private Session session;
    private ObjectMapper objectMapper = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session) {
        log.info("Client connected to {} channel", session.getRequestURI());
        this.session = session;
        this.getListeners().add(this);
    }

    @OnClose
    public void onClose(Session session) {
        log.info("Client disconnected from {} channel", session.getRequestURI());
        getListeners().remove(this);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("");
    }

    public void broadcast(T message) {
        try {
            var messageAsString = objectMapper.writeValueAsString(message);
            getListeners().forEach(listener -> {
                listener.sendMessage(messageAsString);
            });
        } catch (IOException e) {
            log.error("Caught exception while broadcasting {} : {}", message.getClass().getSimpleName(), message, e);
        }
    }


    public void disconnect() {
        getListeners().forEach(listener -> {
            try {
                listener.session.close();
            } catch (IOException ignored) {
            }
        });
    }

    void sendMessage(String message) {
        try {
            this.session.getAsyncRemote().sendText(message);
        } catch (Exception e) {
            log.error("Caught exception while sending message to Session " + this.session.getId(), e.getMessage(), e);
        }
    }

    protected abstract Set<WsServer<T>> getListeners();
}
