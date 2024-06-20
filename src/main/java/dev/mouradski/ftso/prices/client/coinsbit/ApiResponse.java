package dev.mouradski.ftso.prices.client.coinsbit;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ApiResponse {
    private int code;
    private boolean success;
    private String message;
    private Map<String, TickerData> result;
}
