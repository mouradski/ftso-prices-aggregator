package dev.mouradski.prices.service;

import dev.mouradski.prices.model.Trade;
import dev.mouradski.prices.server.TradeServer;
import dev.mouradski.prices.service.TradeConsummer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class PriceService {

    private final Optional<TradeServer> tradeServer;
    private final Optional<TradeConsummer> tradeConsummer;

    public PriceService(@Autowired(required = false) TradeServer tradeServer, @Autowired(required = false) TradeConsummer tradeConsummer) {
        this.tradeServer = Optional.ofNullable(tradeServer);
        this.tradeConsummer = Optional.ofNullable(tradeConsummer);
    }

    public void pushPrice(Trade trade) {
        tradeServer.ifPresent(server -> server.broadcastTrade(trade));
        tradeConsummer.ifPresent(consummer -> consummer.processTrade(trade));
    }
}
