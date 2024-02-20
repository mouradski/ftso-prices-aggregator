package dev.mouradski.ftso.prices.client.bitmake;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TickerPayload {
    private String tp;
    private String e;
    private ProductSymbol ps;
    private List<BitmakeTicker> d;
    private boolean f;
}
