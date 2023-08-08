package dev.mouradski.ftso.trades.client.bitforex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TradeEvent {
    private List<Item> data;
    private String event;
    private Param param;
}

@Getter
@Setter
class Item {
    private double price;
    private double amount;
    private long time;
    private int direction;
    @JsonProperty("tid")
    private String transactionId;
}

@Getter
@Setter
class Param {
    @JsonProperty("businessType")
    private String businessType;
}
