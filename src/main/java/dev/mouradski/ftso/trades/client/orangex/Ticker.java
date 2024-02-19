package dev.mouradski.ftso.trades.client.orangex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class Ticker {
    public Params params;
    public String method;
    public String jsonrpc;
}

@Getter
@Setter
class Params {
    public Data data;
    public String channel;
}

@Getter
@Setter
class Data {
    public String timestamp;
    public Stats stats;
    public String state;
    @JsonProperty("last_price")

    public String lastPrice;
    @JsonProperty("instrument_name")
    public String instrumentName;
    @JsonProperty("mark_price")
    public String markPrice;
    @JsonProperty("best_bid_price")
    public String bestBidPrice;
    @JsonProperty("best_bid_amount")
    public String bestBidAmount;
    @JsonProperty("best_ask_price")
    public String bestAskPrice;
    @JsonProperty("best_ask_amount")
    public String bestAskAmount;
}

@Getter
@Setter
class Stats {
    public String volume;
    @JsonProperty("price_change")
    public String priceChange;
    public String low;
    public String turnover;
    public String high;
}
