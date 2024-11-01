package dev.mouradski.ftso.prices.client.tidex;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MarketData {
    private long at;
    private Ticker ticker;
}
