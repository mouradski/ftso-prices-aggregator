package dev.mouradski.ftso.trades.client.btcex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class TradeData {
    String timestamp;
    Double price;
    Double amount;
    String direction;
    @JsonProperty("instrument_name")
    String instrumentName;
    @JsonProperty("trade_id")
    String tradeId;
    @JsonProperty("mark_price")
    Double markPrice;
}
