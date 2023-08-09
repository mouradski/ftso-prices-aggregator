package dev.mouradski.ftso.trades.client.bingx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TradeData {
    private boolean m;
    @JsonProperty("p")
    private String price;
    @JsonProperty("q")
    private String amount;
    @JsonProperty("s")
    private String symbol;
    @JsonProperty("t")
    private String tId;
}
