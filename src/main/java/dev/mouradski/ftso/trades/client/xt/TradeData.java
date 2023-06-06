package dev.mouradski.ftso.trades.client.xt;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TradeData {

    @JsonProperty("s")
    private String symbol;
    
    @JsonProperty("i")
    private String id;
    
    @JsonProperty("t")
    private Long time;
    
    @JsonProperty("p")
    private Double price;
    
    @JsonProperty("q")
    private Double quantity;
    
    @JsonProperty("b")
    private Boolean isBuyer;
}
