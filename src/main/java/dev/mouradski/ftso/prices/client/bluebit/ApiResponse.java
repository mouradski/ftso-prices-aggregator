package dev.mouradski.ftso.prices.client.bluebit;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiResponse {
    private String code;
    private String msg;
    private Data data;
}

@Getter
@Setter
class Data {
    private List<Ticker> ticker;
}

@Getter
@Setter
class Ticker {
    private String symbol;
    private int newCoinFlag;
    private String amount;
    private String high;
    private String vol;
    private double last;
    private String low;
    private double buy;
    private double sell;
    private String change;
    private String rose;
    private int isShow;
}
