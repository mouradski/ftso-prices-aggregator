package dev.mouradski.ftso.trades.service;

import dev.mouradski.ftso.trades.model.Ticker;

public interface TickerConsumer {
    void processTicker(Ticker trade);
}
