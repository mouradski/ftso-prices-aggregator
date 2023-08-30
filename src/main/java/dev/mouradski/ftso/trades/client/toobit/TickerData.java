package dev.mouradski.ftso.trades.client.toobit;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TickerData {

    private String symbol;
    private String symbolName;
    private String topic;
    private Params params;
    private List<DataEntry> data;
    private boolean f;
    private long sendTime;
    private boolean shared;

    @Getter
    @Setter
    public static class Params {
        private String realtimeInterval;
        private String binary;
    }

    @Getter
    @Setter
    public static class DataEntry {
        private long t;
        private String s;
        private String sn;
        private Double c;
        private String h;
        private String l;
        private String o;
        private String v;
        private String qv;
        private String m;
        private int e;
    }
}
