package dev.mouradski.ftso.prices.client.bitget;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TickerMessage {
    private String action;
    private Arg arg;
    private List<Data> data;
    private long ts;
}
