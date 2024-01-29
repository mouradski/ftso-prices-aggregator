package dev.mouradski.ftso.trades.client.coinw;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class TickersResponse {
    private String code;
    private Map<String, CryptoData> data;
    private boolean failed;
    private String msg;
    private boolean success;
}
