package dev.mouradski.ftso.trades.service;

import dev.mouradski.ftso.trades.model.Trade;

public interface TradeConsummer {
    void processTrade(Trade trade);
}
