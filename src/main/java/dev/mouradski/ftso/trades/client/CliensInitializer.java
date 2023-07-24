package dev.mouradski.ftso.trades.client;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CliensInitializer {

    public CliensInitializer(List<AbstractClientEndpoint> clients) {
        clients.parallelStream().forEach(AbstractClientEndpoint::start);
    }
}
