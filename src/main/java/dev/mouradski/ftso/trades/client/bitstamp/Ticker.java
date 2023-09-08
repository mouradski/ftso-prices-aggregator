package dev.mouradski.ftso.trades.client.bitstamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ticker {

    private Double last;
    private String pair;
}
