package dev.mouradski.ftso.trades.client.pionex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class TradeData {

    private String symbol;
    @JsonProperty("tradeId")
    private String tradeId;
    private Double price;
    private Double size;
    private String side;
    private long timestamp;
}
