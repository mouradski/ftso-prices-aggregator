package dev.mouradski.ftso.prices.client.trubit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TickerData {
    private String symbol;
    private String lastPrice;

    @JsonProperty("base_currency")
    private String base;

    @JsonProperty("quote_currency")
    private String quote;
}
