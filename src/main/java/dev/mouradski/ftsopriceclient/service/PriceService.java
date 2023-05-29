package dev.mouradski.ftsopriceclient.service;

import dev.mouradski.ftsopriceclient.model.Trade;
import dev.mouradski.ftsopriceclient.server.TradeServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class PriceService {

    private final TradeServer tradeServer;

    public PriceService(TradeServer tradeServer) throws IOException {
        this.tradeServer = tradeServer;
    }

    public void pushPrice(Trade trade) {
        tradeServer.broadcastTrade(trade);
    }
}
