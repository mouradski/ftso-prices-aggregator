package dev.mouradski.ftsopriceclient.service;

import dev.mouradski.ftsopriceclient.model.Trade;
import dev.mouradski.ftsopriceclient.server.TradeServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
