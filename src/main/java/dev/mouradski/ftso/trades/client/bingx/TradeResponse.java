package dev.mouradski.ftso.trades.client.bingx;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TradeResponse {
    private int code;
    private TradeData data;
    @JsonProperty("dataType")
    private String dataType;
    private boolean success;
}
