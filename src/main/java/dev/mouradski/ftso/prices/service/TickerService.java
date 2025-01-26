package dev.mouradski.ftso.prices.service;

import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.server.TickerServer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@ApplicationScoped
public class TickerService {

    @Inject
    TickerServer tickerServer;
    @Inject
    Instance<TickerConsumer> tickerConsumer;

    @Inject
    Instance<ErrorConsumer> errorConsumers;

    @ConfigProperty(name = "print.data")
    boolean printData;

    public void pushTicker(Ticker ticker) {
        if (printData) {
            log.info("{}", ticker);
        }
        tickerServer.broadcast(ticker);
        tickerConsumer.forEach(consumer -> consumer.processTicker(ticker));
    }

    public void pushError(String exchange) {
        errorConsumers.forEach(consumer -> consumer.processError(exchange));
    }
}
