package dev.mouradski.ftso.prices.service;

import dev.mouradski.ftso.prices.model.Ticker;

public interface TickerConsumer {
    void processTicker(Ticker trade);
}
