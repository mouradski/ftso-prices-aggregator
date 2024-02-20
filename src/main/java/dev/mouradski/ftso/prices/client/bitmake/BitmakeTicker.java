package dev.mouradski.ftso.prices.client.bitmake;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BitmakeTicker {
    private String s;
    private long t;
    private String c;
    private Double h;
    private Double l;
    private Double o;
    private Double v;
    private String qv;
    private String m;
}
