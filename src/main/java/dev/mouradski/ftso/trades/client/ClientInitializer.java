package dev.mouradski.ftso.trades.client;

import dev.mouradski.ftso.trades.service.TradeService;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

@ApplicationScoped

@Startup
public class ClientInitializer {

    @Inject
    @Any
    Instance<AbstractClientEndpoint> clients;

    @Inject
    TradeService priceSender;

    @ConfigProperty(name = "exchanges")
    List<String> exchanges;

    @ConfigProperty(name = "assets")
    List<String> assets;

    @PostConstruct
    public void init() {
        clients.stream().parallel().forEach(
                client -> client.start(priceSender, exchanges, assets)
        );
    }
}
