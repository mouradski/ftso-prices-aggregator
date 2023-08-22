package dev.mouradski.ftso.trades.client.kucoin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TickerMessage {
    @JsonProperty("topic")
    private String topic;

    @JsonProperty("type")
    private String type;

    @JsonProperty("data")
    private TickerData data;

    @JsonProperty("subject")
    private String subject;
}

@Getter
@Setter
class TickerData {
    @JsonProperty("bestAsk")
    private String bestAsk;

    @JsonProperty("bestAskSize")
    private String bestAskSize;

    @JsonProperty("bestBid")
    private String bestBid;

    @JsonProperty("bestBidSize")
    private String bestBidSize;

    @JsonProperty("price")
    private Double price;

    @JsonProperty("sequence")
    private String sequence;

    @JsonProperty("size")
    private String size;

    @JsonProperty("time")
    private long time;
}
