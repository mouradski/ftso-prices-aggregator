package dev.mouradski.ftso.trades.client.bitforex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TickerEvent {

    private TickerData data;
    private String event;
    private TickerParam param;


    @Getter
    @Setter
    public static class TickerData {
        private long startdate;
        private long enddate;
        private String type;
        private double high;
        private double low;
        private double last;
        private double open;
        private double productvol;
        private double currencyvol;
        private double allVol;
        private double allCurrencyVol;
        private double rate;

    }

    @Getter
    @Setter
    public static class TickerParam {
        @JsonProperty("businessType")
        private String businessType;
    }
}
