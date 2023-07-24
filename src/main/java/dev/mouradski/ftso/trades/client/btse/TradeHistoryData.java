package dev.mouradski.ftso.trades.client.btse;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class TradeHistoryData {
    private String symbol;
    private String side;
    private double size;
    private double price;
    @JsonProperty("tradeId")
    private long tradeId;
    @JsonProperty("timestamp")
    private long timestamp;
}
