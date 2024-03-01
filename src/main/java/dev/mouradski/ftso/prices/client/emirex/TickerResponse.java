package dev.mouradski.ftso.prices.client.emirex;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TickerResponse {
    private boolean status;
    private Ticker data; 
}

@Getter
@Setter
class Ticker {
    private String id;
    private String pair;
    private Double last;
    private Double open;
    private Double close;
    private Double high;
    private Double low;
    private String volume_24H;
    private String min_trade;
}
