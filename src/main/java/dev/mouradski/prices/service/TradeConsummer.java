package dev.mouradski.prices.service;

import dev.mouradski.prices.model.Trade;

public interface TradeConsummer {
    void processTrade(Trade trade);
}
