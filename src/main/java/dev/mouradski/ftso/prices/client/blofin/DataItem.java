package dev.mouradski.ftso.prices.client.blofin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class DataItem {
    private String instId;
    private String last;
    @JsonProperty("lastSize")
    private String lastSize;
    @JsonProperty("askPrice")
    private String askPrice;
    @JsonProperty("askSize")
    private String askSize;
    @JsonProperty("bidPrice")
    private String bidPrice;
    @JsonProperty("bidSize")
    private String bidSize;
    @JsonProperty("open24h")
    private String open24h;
    @JsonProperty("high24h")
    private String high24h;
    @JsonProperty("low24h")
    private String low24h;
    @JsonProperty("volCurrency24h")
    private String volCurrency24h;
    @JsonProperty("vol24h")
    private String vol24h;
    private String ts;
}
