package dev.mouradski.ftso.trades.client.coinex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class TickerResponse {
    private int code;
    private Data data;
}

@Getter
@Setter
class Data {
    private long date;
    private Map<String, TickerDetails> ticker;
}

@Getter
@Setter
class TickerDetails {
    private String vol;
    private String low;
    private String open;
    private String high;
    private Double last;
    private String buy;
    @JsonProperty("buy_amount")
    private String buyAmount;
    private String sell;
    @JsonProperty("sell_amount")
    private String sellAmount;
}
