package dev.mouradski.ftso.prices.client.bitso;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Map;

@Getter
public class PayloadItem {
    private String high;
    private String last;
    @JsonProperty("created_at")
    private String createdAt;
    private String book;
    private String volume;
    private String vwap;
    private String low;
    private String ask;
    private String bid;
    @JsonProperty("change_24")
    private String change24;
    @JsonProperty("rolling_average_change")
    private Map<String, String> rollingAverageChange;
}

