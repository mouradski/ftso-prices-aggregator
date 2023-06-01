package dev.mouradski.prices.service;

import dev.mouradski.prices.model.Trade;
import dev.mouradski.prices.server.TradeServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class PriceService {

    private final TradeServer tradeServer;
    private final TradeConsummer tradeConsummer;

    public PriceService(@Autowired(required = false) TradeServer tradeServer, @Autowired(required = false) TradeConsummer tradeConsummer) throws IOException {
        this.tradeServer = tradeServer;
        this.tradeConsummer = tradeConsummer;
    }

    public void pushPrice(Trade trade) {
        if (this.tradeServer != null) {
            this.tradeServer.broadcastTrade(trade);
        }

        if (this.tradeConsummer != null) {
            this.tradeConsummer.processTrade(trade);
        }
    }
}
