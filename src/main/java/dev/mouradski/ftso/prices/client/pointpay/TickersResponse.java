package dev.mouradski.ftso.prices.client.pointpay;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class TickersResponse {
    private int code;
    private boolean success;
    private String message;
    private Map<String, TickerInfo> result;
}

@Getter
@Setter
class TickerInfo {
    private long at;
    private Ticker ticker;
}

@Getter
@Setter
class Ticker {
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
