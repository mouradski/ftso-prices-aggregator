package dev.mouradski.ftso.prices.client.koinbay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TickerMsg {
    private String channel;
    private Tick tick;
}
