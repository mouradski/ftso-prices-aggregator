package dev.mouradski.ftso.prices.client.bybit;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TickerResponse {

    private int retCode;
    private String retMsg;
    private Result result;
    private Object retExtInfo;
    private long time;

    @Getter
    @Setter
    public static class Result {
        private String category;
        private List<Ticker> list;
    }

    @Getter
    @Setter
    public static class Ticker {
        private String symbol;
        private String bid1Price;
        private String bid1Size;
        private String ask1Price;
        private String ask1Size;
        private Double lastPrice;
        private String prevPrice24h;
        private String price24hPcnt;
        private String highPrice24h;
        private String lowPrice24h;
        private String turnover24h;
        private String volume24h;
        private String usdIndexPrice;
    }
}
