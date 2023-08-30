package dev.mouradski.ftso.trades.client.coinbase;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ticker {
    private Double price;
    @JsonProperty("product_id")
    private String productId;
}
