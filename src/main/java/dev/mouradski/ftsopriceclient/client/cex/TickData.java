package dev.mouradski.ftsopriceclient.client.cex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TickData {

    @JsonProperty("symbol1")
    private String symbol1;

    @JsonProperty("symbol2")
    private String symbol2;

    @JsonProperty("price")
    private Double price;

    @JsonProperty("open24")
    private String open24;

    @JsonProperty("volume")
    private Double volume;
}


