package dev.mouradski.ftso.prices.client.indoex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TickerData {
    @JsonProperty("base_currency")
    private String base;

    @JsonProperty("target_currency")
    private String quote;

    @JsonProperty("last_price")
    private String lastPrice;

}
