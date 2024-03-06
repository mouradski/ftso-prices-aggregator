package dev.mouradski.ftso.prices.client.bitget;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TickerMessage {
    private String action;
    private Arg arg;
    private List<Data> data;
    private long ts;
}
