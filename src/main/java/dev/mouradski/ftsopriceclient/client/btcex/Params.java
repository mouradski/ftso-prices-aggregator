package dev.mouradski.ftsopriceclient.client.btcex;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Params {
    private String channel;
    private BtcexData data;
}
