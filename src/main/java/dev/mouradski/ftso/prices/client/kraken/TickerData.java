package dev.mouradski.ftso.prices.client.kraken;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TickerData {
    private String symbol;
    private Double last;

    @JsonProperty("product_id")
    private String productId;
}
