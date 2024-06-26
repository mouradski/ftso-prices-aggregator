package dev.mouradski.ftso.prices.client.azbit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TickerData {

    @JsonProperty("currencyPairCode")
    private String symbol;
    private Double price;
}
