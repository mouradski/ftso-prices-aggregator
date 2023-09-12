package dev.mouradski.ftso.trades.client.cexio;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TickerResponse {
    
    @JsonProperty("e")
    private String e;

    @JsonProperty("ok")
    private String ok;

    @JsonProperty("data")
    private List<TickerData> data;

    @Getter
    @Setter
    public static class TickerData {
        
        @JsonProperty("timestamp")
        private String timestamp;

        @JsonProperty("pair")
        private String pair;

        @JsonProperty("low")
        private String low;

        @JsonProperty("high")
        private String high;

        @JsonProperty("last")
        private String last;

        @JsonProperty("volume")
        private String volume;

        @JsonProperty("volume30d")
        private String volume30d;

        @JsonProperty("priceChange")
        private String priceChange;

        @JsonProperty("priceChangePercentage")
        private String priceChangePercentage;

        @JsonProperty("bid")
        private double bid;

        @JsonProperty("ask")
        private double ask;

        // getters, setters, toString...
    }
}
