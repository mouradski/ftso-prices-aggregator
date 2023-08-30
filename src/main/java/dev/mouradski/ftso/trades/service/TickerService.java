package dev.mouradski.ftso.trades.service;

import dev.mouradski.ftso.trades.model.Ticker;
import dev.mouradski.ftso.trades.server.TickerServer;
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
        tickerServer.broadcast(ticker);
        tickerConsumer.forEach(consumer -> consumer.processTicker(ticker));
    }
}
