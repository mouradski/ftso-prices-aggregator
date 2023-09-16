package dev.mouradski.ftso.trades.client.bitmake;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BitmakeTrade {
    private String v;
    private long t;
    private Double p;
    private Double q;
    private boolean m;
}
