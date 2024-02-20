package dev.mouradski.ftso.prices.client.digifinex;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TickerData {
    private double vol;
    private double change;
    private double base_vol;
    private double sell;
    private double last;
    private String symbol;
    private double low;
    private double buy;
    private double high;

}
