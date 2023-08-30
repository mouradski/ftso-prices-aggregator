package dev.mouradski.ftso.trades.client.lbank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TickerData {

    private TickData tick;
    private String type;
    private String pair;
    private String TS;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TickData {

        @JsonProperty("to_cny")
        private double toCny;
        private double high;
        private double vol;
        private double low;
        private double change;
        private double usd;
        @JsonProperty("to_usd")
        private double toUsd;
        private String dir;
        private double turnover;
        private double latest;
        private double cny;
    }
}
