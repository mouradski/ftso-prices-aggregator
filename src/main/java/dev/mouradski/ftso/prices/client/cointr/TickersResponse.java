package dev.mouradski.ftso.prices.client.cointr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TickersResponse {
    private TickerData[] data;
}
