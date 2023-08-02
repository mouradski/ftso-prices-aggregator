package dev.mouradski.ftso.trades.service;

import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.server.TradeServer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@ApplicationScoped
public class TradeService {

    @Inject
    Instance<TradeServer> tradeServer;
    @Inject
    Instance<TradeConsummer> tradeConsumer;

    public void pushTrade(Trade trade) {

        tradeServer.forEach(server -> server.broadcastTrade(trade));

        tradeConsumer.forEach(consumer -> consumer.processTrade(trade));
    }
}
