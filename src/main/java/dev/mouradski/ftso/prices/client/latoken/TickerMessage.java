package dev.mouradski.ftso.prices.client.latoken;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TickerMessage {
    private String symbol;
    private String lastPrice;
}
