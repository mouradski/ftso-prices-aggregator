package dev.mouradski.ftso.prices.client.toobit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
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
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Params {
        private String realtimeInterval;
        private String binary;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
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
