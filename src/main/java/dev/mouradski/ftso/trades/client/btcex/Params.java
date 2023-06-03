package dev.mouradski.ftso.trades.client.btcex;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Params {
    private String channel;
    private BtcexData data;
}
