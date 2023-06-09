package dev.mouradski.ftso.trades.client.btse;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
class TradeHistoryResponse {

    private String topic;
    private List<TradeHistoryData> data;
}
