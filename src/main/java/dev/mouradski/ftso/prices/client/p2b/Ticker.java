package dev.mouradski.ftso.prices.client.p2b;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Ticker {
    private Double bid;
    private Double ask;
    private Double low;
    private Double high;
    private Double last;
    private Double vol;
    private String deal;
    private String change;
}
