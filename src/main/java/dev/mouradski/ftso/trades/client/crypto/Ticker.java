package dev.mouradski.ftso.trades.client.crypto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Ticker {

    private int id;
    private int code;
    private String method;
    private ResultData result;

    @Getter
    @Setter
    public static class ResultData {

        private String channel;
        private String instrument_name;
        private String subscription;
        private int id;
        private List<TickerData> data;
    }

    @Getter
    @Setter
    public static class TickerData {

        private String h;
        private String l;
        private Double a;
        private Double c;
        private String b;
        private String bs;
        private String k;
        private String ks;
        private String i;
        private String v;
        private String vv;
        private String oi;
        private long t;
    }
}
