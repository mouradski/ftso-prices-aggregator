package dev.mouradski.ftso.trades.client.bitget;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TickerResponse {

    private String action;
    private Argument arg;
    private Data[] data;
    private long ts;

    @Getter
    @Setter
    public static class Argument {
        private String instType;
        private String channel;
        private String instId;
    }

    @Getter
    @Setter
    public static class Data {
        private String instId;
        private Double last;
        private String open24h;
        private String high24h;
        private String low24h;
        private String bestBid;
        private String bestAsk;
        private String baseVolume;
        private String quoteVolume;
        private long ts;
        private int labeId;
        private String openUtc;
        private String chgUTC;
        private String bidSz;
        private String askSz;
    }
}
