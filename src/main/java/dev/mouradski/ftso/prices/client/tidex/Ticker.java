package dev.mouradski.ftso.prices.client.tidex;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Ticker {
    private String name;
    private Double bid;
    private Double ask;
    private Double open;
    private Double high;
    private Double low;
    private Double last;
    private Double vol;
    private Double deal;
    private Double change;
}
