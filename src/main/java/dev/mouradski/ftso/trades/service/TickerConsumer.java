package dev.mouradski.ftso.trades.service;

import dev.mouradski.ftso.trades.model.Ticker;
import dev.mouradski.ftso.trades.model.Trade;

public interface TickerConsumer {
    void processTicker(Ticker ticker);
}
