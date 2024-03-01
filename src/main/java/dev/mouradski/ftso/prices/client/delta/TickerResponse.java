package dev.mouradski.ftso.prices.client.delta;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TickerResponse {
    private String symbol;
    private Double close;
}
