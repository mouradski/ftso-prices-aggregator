package dev.mouradski.ftso.trades.client.lbank;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Trade {

    @JsonProperty("volume")
    private Double volume;

    @JsonProperty("amount")
    private Double amount;

    @JsonProperty("price")
    private Double price;

    @JsonProperty("direction")
    private String direction;

    @JsonProperty("TS")
    private String ts;
}
