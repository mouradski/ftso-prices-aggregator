package dev.mouradski.ftso.trades.service;

import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.server.TradeServer;
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
