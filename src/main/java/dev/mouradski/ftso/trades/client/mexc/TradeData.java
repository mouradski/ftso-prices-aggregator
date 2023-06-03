package dev.mouradski.ftso.trades.client.mexc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TradeData {
    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("data")
    private Data data;

    @JsonProperty("channel")
    private String channel;
}
