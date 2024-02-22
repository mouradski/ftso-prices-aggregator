package dev.mouradski.ftso.prices.client.tapbit;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TickerData {
    private String symbol;
    private String lastPrice;
    private String bestAskPrice;
    private String bestBidPrice;
    private String high24h;
    private String open24h;
    private String openPrice;
    private String low24h;
    private String volume24h;
    private long timestamp;
}
