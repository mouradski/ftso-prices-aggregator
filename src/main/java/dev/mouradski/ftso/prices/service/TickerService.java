package dev.mouradski.ftso.prices.service;

import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.server.TickerServer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class TickerService {

    @Inject
    TickerServer tickerServer;
    @Inject
    Instance<TickerConsumer> tickerConsumer;

    public void pushTicker(Ticker ticker) {
        //System.out.println(ticker);
        tickerServer.broadcast(ticker);
        tickerConsumer.forEach(consumer -> consumer.processTicker(ticker));
    }
}
