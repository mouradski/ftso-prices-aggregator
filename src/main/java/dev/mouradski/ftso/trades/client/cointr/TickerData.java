package dev.mouradski.ftso.trades.client.cointr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TickerData {
    private String lastPx;
    private String instId;
}
