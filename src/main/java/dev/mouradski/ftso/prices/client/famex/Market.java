package dev.mouradski.ftso.prices.client.famex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Market {
    @JsonProperty("base_id")
    private String base;
    @JsonProperty("quote_id")
    private String quote;
    @JsonProperty("last_price")
    private String lastPrice;
}
