package dev.mouradski.ftso.prices.client.kucoin;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TickerData {
    private long time;
    private List<Ticker> ticker;
}
