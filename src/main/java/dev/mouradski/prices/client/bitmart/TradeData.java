package dev.mouradski.prices.client.bitmart;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TradeData {
    @JsonProperty("price")
    private Double price;
    
    @JsonProperty("s_t")
    private long time;
    
    @JsonProperty("side")
    private String side;
    
    @JsonProperty("size")
    private Double size;
    
    @JsonProperty("symbol")
    private String symbol;
}

