package dev.mouradski.ftso.trades.client.pionex;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
class TradeResponse {

    private String topic;
    private String symbol;
    private List<TradeData> data;
    private long timestamp;
}
