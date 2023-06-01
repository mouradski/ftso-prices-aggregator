package dev.mouradski.prices.client.coinbase;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TradeMatch {

    @JsonProperty("type")
    private String type;

    @JsonProperty("trade_id")
    private long tradeId;

    @JsonProperty("maker_order_id")
    private String makerOrderId;

    @JsonProperty("taker_order_id")
    private String takerOrderId;

    @JsonProperty("side")
    private String side;

    @JsonProperty("size")
    private Double size;

    @JsonProperty("price")
    private Double price;

    @JsonProperty("product_id")
    private String productId;

    @JsonProperty("sequence")
    private long sequence;

    @JsonProperty("time")
    private String time;
}
