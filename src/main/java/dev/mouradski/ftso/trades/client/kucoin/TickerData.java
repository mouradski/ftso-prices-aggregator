package dev.mouradski.ftso.trades.client.kucoin;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TickerData {
    private long time;
    private List<Ticker> ticker;
}
