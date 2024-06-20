package dev.mouradski.ftso.prices.client.coinsbit;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Ticker {
    private String name;
    private String bid;
    private String ask;
    private String open;
    private String high;
    private String low;
    private String last;
    private String vol;
    private String deal;
    private String change;

}
