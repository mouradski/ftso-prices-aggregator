package dev.mouradski.ftso.prices.client.bingx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TickerData {
    @JsonProperty("c")
    private String lastPrice;
    @JsonProperty("s")
    private String symbol;
}
