package dev.mouradski.ftso.prices.client.bingx;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TickerResponse {
    private int code;
    private TickerData data;
    @JsonProperty("dataType")
    private String dataType;
    private boolean success;
}
