package dev.mouradski.ftso.trades.client.latoken;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)

public class Data {
    private String symbol;

    private Double lastPrice;
}
