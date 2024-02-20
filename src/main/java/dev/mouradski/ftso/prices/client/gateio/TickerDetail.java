package dev.mouradski.ftso.prices.client.gateio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TickerDetail {

    @JsonProperty("currency_pair")
    private String currencyPair;

    private Double last;
}
