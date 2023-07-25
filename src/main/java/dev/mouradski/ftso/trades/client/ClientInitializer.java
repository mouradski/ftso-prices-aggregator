package dev.mouradski.ftso.trades.client;

import dev.mouradski.ftso.trades.service.TradeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClientInitializer {

    public ClientInitializer(List<AbstractClientEndpoint> clients, TradeService priceSender, @Value("${exchanges}") List<String> exchanges, @Value("${assets}") List<String> assets) {
        clients.parallelStream().forEach(client -> client.configure(priceSender, exchanges, assets));
        clients.parallelStream().forEach(AbstractClientEndpoint::start);
    }
}
