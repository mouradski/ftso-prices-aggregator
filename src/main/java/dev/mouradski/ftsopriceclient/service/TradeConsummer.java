package dev.mouradski.ftsopriceclient.service;

import dev.mouradski.ftsopriceclient.model.Trade;

public interface TradeConsummer {
    void processTrade(Trade trade);
}
