package dev.mouradski.ftso.trades.client.bybit;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class TradeResponse {
    private Params params;
    private String topic;
    private Map<String, String> data;
}
