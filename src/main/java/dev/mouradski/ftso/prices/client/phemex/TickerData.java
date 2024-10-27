package dev.mouradski.ftso.prices.client.phemex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TickerData {


    private String symbol;

    @JsonProperty("lastEp")
    private Long lastPrice;
}
