package dev.mouradski.prices.client.upbit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpbitTrade {

    @JsonProperty("type")
    private String type;

    @JsonProperty("code")
    private String code;

    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("trade_date")
    private String tradeDate;

    @JsonProperty("trade_time")
    private String tradeTime;

    @JsonProperty("trade_timestamp")
    private Long tradeTimestamp;

    @JsonProperty("trade_price")
    private Double tradePrice;

    @JsonProperty("trade_volume")
    private Double tradeVolume;

    @JsonProperty("ask_bid")
    private String askBid;

    @JsonProperty("prev_closing_price")
    private Double prevClosingPrice;

    @JsonProperty("change")
    private String change;

    @JsonProperty("change_price")
    private Double changePrice;

    @JsonProperty("sequential_id")
    private Long sequentialId;

    @JsonProperty("stream_type")
    private String streamType;
}
