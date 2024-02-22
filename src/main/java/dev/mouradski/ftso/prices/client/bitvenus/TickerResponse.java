package dev.mouradski.ftso.prices.client.bitvenus;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TickerResponse {
    private String symbol;
    private String symbolName;
    private String topic;
    private Params params;
    private List<Data> data;
    private boolean f;
    private long sendTime;
    private boolean shared;
}
