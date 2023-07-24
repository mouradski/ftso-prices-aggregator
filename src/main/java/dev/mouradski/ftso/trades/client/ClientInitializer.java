package dev.mouradski.ftso.trades.client;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClientInitializer {

    public ClientInitializer(List<AbstractClientEndpoint> clients) {
        clients.parallelStream().forEach(AbstractClientEndpoint::start);
    }
}
