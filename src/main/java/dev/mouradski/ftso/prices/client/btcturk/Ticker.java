package dev.mouradski.ftso.prices.client.btcturk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ticker {
    private String pair;
    private Double last;
}
