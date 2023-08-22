package dev.mouradski.ftso.trades.client.pionex;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TickerData {
    private boolean result;
    private Data data;

    @Getter
    @Setter
    public static class Data {
        private List<Ticker> tickers;
    }

    @Getter
    @Setter
    public static class Ticker {
        private String symbol;
        private long time;
        private String open;
        private Double close;
        private String low;
        private String high;
        private String volume;
        private String amount;
        private int count;
    }
}
