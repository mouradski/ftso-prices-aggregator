package dev.mouradski.ftso.prices.client.cexio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class TickerResponse {

    @JsonProperty("ok")
    private String ok;

    @JsonProperty("data")
    private Map<String, TickerData> data;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    public static class TickerData {

        @JsonProperty("bestBid")
        private String bestBid;

        @JsonProperty("bestAsk")
        private String bestAsk;

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
    }

}
