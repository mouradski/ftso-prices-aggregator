package dev.mouradski.ftso.trades.client;

import dev.mouradski.ftso.trades.service.TradeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClientInitializer {

    public ClientInitializer(List<AbstractClientEndpoint> clients, TradeService priceSender, @Value("${exchanges}") List<String> exchanges, @Value("${assets}") List<String> assets, @Value("${subscribe.trade:false}") Boolean subscribeTrade, @Value("${subscribe.ticker:true}") Boolean subscribeTicker) {
        clients.parallelStream().forEach(client -> client.start(priceSender, exchanges, assets, subscribeTrade, subscribeTicker));
    }
}
