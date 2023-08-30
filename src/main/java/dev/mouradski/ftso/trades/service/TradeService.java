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

    @Inject
    VolumesService volumesService;

    public void pushTrade(Trade trade) {
        var weights = volumesService.updateVolumes(trade.getExchange(), trade.getBase(), trade.getQuote(), trade.getAmount());
        trade.setVolumeWeightByExchangeBaseQuote(weights.getLeft());
        trade.setVolumeWeightByExchangeBase(weights.getRight());
        tradeServer.broadcast(trade);
        tradeConsumer.forEach(consumer -> consumer.processTrade(trade));
    }
}
