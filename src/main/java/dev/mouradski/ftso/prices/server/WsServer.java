package dev.mouradski.ftso.prices.server;

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
    private ObjectMapper objectMapper = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session) {
        log.info("Client connected to {} channel", session.getRequestURI());
        this.getSessions().add(session);
    }

    @OnClose
    public void onClose(Session session) {
        log.info("Client disconnected from {} channel", session.getRequestURI());
        getSessions().remove(this);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("");
    }

    public void broadcast(T message) {
        try {
            var messageAsString = objectMapper.writeValueAsString(message);
            getSessions().forEach(listener -> {
                try {
                    listener.getAsyncRemote().sendText(messageAsString);
                } catch (Exception e) {
                    log.error("Caught exception while sending message to Session " + listener.getId(), e.getMessage(), e);
                }
            });
        } catch (IOException e) {
            log.error("Caught exception while broadcasting {} : {}", message.getClass().getSimpleName(), message, e);
        }
    }


    public void disconnect() {
        getSessions().forEach(listener -> {
            try {
                listener.close();
            } catch (IOException ignored) {
            }
        });
    }


    protected abstract Set<Session> getSessions();
}
