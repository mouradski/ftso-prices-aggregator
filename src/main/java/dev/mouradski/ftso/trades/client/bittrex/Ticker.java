package dev.mouradski.ftso.trades.client.bittrex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ticker {
    private String symbol;
    private String lastTradeRate;
}
