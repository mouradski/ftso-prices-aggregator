package dev.mouradski.ftso.prices.client.gateio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeteIOTicker {
    private String channell;
    private String event;
    private TickerDetail result;
}
