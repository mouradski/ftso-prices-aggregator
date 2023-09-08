package dev.mouradski.ftso.trades.client.kucoin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Ticker {
    private String symbol;
    private String symbolName;
    private String buy;
    private String sell;
    private Double changeRate;
    private Double changePrice;
    private Double high;
    private Double low;
    private Double vol;
    private Double volValue;
    private Double last;
    private Double averagePrice;
    private String takerFeeRate;
    private String makerFeeRate;
    private String takerCoefficient;
    private String makerCoefficient;
}
