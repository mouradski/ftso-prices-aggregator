package dev.mouradski.ftso.trades.service;

import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.server.TradeServer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@ApplicationScoped
public class TradeService {

    @Inject
    TradeServer tradeServer;
    @Inject
    Instance<TradeConsummer> tradeConsumer;

    public void pushTrade(Trade trade) {
        tradeServer.broadcastTrade(trade);
        tradeConsumer.forEach(consumer -> consumer.processTrade(trade));
    }

    public void setTradeServer(TradeServer tradeServer) {
        this.tradeServer = tradeServer;
    }
}
