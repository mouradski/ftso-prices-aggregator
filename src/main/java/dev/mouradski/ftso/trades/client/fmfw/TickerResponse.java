package dev.mouradski.ftso.trades.client.fmfw;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class TickerResponse {
    @JsonProperty("ch")
    private String channel;

    @JsonProperty("data")
    private Map<String, TickerData> data;
}

@Getter
@Setter
class TickerData {
    @JsonProperty("t")
    private Long timestamp;

    @JsonProperty("o")
    private String openPrice;

    @JsonProperty("c")
    private Double closePrice;

    @JsonProperty("h")
    private String highPrice;

    @JsonProperty("l")
    private String lowPrice;

    @JsonProperty("v")
    private String volume;

    @JsonProperty("q")
    private String quoteVolume;
}
