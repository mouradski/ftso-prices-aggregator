package dev.mouradski.ftso.prices.client.bitget;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class Data {
    @JsonProperty("instId")
    private String instId;
    @JsonProperty("lastPr")
    private String lastPr;
    @JsonProperty("open24h")
    private String open24h;
    @JsonProperty("high24h")
    private String high24h;
    @JsonProperty("low24h")
    private String low24h;
    @JsonProperty("change24h")
    private String change24h;
    @JsonProperty("bidPr")
    private String bidPr;
    @JsonProperty("askPr")
    private String askPr;
    @JsonProperty("bidSz")
    private String bidSz;
    @JsonProperty("askSz")
    private String askSz;
    @JsonProperty("baseVolume")
    private String baseVolume;
    @JsonProperty("quoteVolume")
    private String quoteVolume;
    @JsonProperty("openUtc")
    private String openUtc;
    @JsonProperty("changeUtc24h")
    private String changeUtc24h;
    @JsonProperty("ts")
    private String ts;
}
