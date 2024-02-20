package dev.mouradski.ftso.prices.client.okex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TickerInfo {

    @JsonProperty("arg")
    private Argument arg;

    @JsonProperty("data")
    private List<TickerData> data;
}

@Getter
@Setter
class Argument {
    @JsonProperty("channel")
    private String channel;

    @JsonProperty("instId")
    private String instId;
}

@Getter
@Setter
class TickerData {
    @JsonProperty("instId")
    private String instId;

    @JsonProperty("idxPx")
    private Double idxPx;

    @JsonProperty("open24h")
    private String open24h;

    @JsonProperty("high24h")
    private String high24h;

    @JsonProperty("low24h")
    private String low24h;

    @JsonProperty("sodUtc0")
    private String sodUtc0;

    @JsonProperty("sodUtc8")
    private String sodUtc8;

    @JsonProperty("ts")
    private String ts;
}
