package dev.mouradski.ftso.prices.client.nami;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TickerData {
    @JsonProperty("last_price")
    private Double lastPrice;
    private String symbol;
}
