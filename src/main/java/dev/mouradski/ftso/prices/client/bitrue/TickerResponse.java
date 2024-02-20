package dev.mouradski.ftso.prices.client.bitrue;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TickerResponse {

    private TickData tick;
    private String channel;
    private long ts;

    @Getter
    @Setter
    public static class TickData {
        private double amount;
        private double rose;
        private double close;
        private double vol;
        private double high;
        private double low;
        private double open;
    }
}
