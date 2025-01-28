package dev.mouradski.ftso.prices.client.bibox;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TickerData {

    @JsonProperty("coin_symbol")
    private String base;
    @JsonProperty("currency_symbol")
    private String quoute;

    private String last;
}
