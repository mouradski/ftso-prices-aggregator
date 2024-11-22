package dev.mouradski.ftso.prices.client.bitrue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TickerData {
    private Double lastPrice;
    private String symbol;
}
