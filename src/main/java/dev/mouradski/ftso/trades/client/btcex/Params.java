package dev.mouradski.ftso.trades.client.btcex;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
class Params {
    String channel;
    List<TradeData> data;
}
