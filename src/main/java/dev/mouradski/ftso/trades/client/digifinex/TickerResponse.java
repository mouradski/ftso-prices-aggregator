package dev.mouradski.ftso.trades.client.digifinex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TickerResponse {

    @JsonProperty("method")
    private String method;

    @JsonProperty("params")
    private List<TickerData> params;

    @JsonProperty("id")
    private Object id;

}

@Getter
@Setter
class TickerData {

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("open_24h")
    private String open24h;

    @JsonProperty("low_24h")
    private String low24h;

    @JsonProperty("high_24h")
    private String high24h;

    @JsonProperty("base_volume_24h")
    private String baseVolume24h;

    @JsonProperty("quote_volume_24h")
    private String quoteVolume24h;

    @JsonProperty("last")
    private Double last;

    @JsonProperty("last_qty")
    private String lastQty;

    @JsonProperty("best_bid")
    private String bestBid;

    @JsonProperty("best_bid_size")
    private String bestBidSize;

    @JsonProperty("best_ask")
    private String bestAsk;

    @JsonProperty("best_ask_size")
    private String bestAskSize;

    @JsonProperty("timestamp")
    private long timestamp;

    // Getters, setters, toString...
}
