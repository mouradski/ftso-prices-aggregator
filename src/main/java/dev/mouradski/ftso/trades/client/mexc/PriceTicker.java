package dev.mouradski.ftso.trades.client.mexc;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PriceTicker {
    private String symbol;
    private Double price;
}
