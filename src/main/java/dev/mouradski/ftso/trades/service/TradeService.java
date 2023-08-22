package dev.mouradski.ftso.trades.service;

import dev.mouradski.ftso.trades.model.Ticker;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.server.TradeServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class TradeService {

    private final Optional<TradeServer> tradeServer;
    private final Optional<TradeConsummer> tradeConsummer;

    private final Optional<TickerConsumer> tickerConsummer;

    public TradeService(@Autowired(required = false) TradeServer tradeServer, @Autowired(required = false) TradeConsummer tradeConsummer, @Autowired(required = false) TickerConsumer tickerConsumer) {
        this.tradeServer = Optional.ofNullable(tradeServer);
        this.tradeConsummer = Optional.ofNullable(tradeConsummer);
        this.tickerConsummer = Optional.ofNullable(tickerConsumer);
    }

    public void pushTrade(Trade trade) {
        tradeServer.ifPresent(server -> server.broadcastTrade(trade));
        tradeConsummer.ifPresent(consummer -> consummer.processTrade(trade));
    }

    public void pushTicker(Ticker ticker) {
        tradeServer.ifPresent(server -> server.broadcastTicker(ticker));
        tickerConsummer.ifPresent(tickerConsumer -> tickerConsumer.processTicker(ticker));
    }
}
