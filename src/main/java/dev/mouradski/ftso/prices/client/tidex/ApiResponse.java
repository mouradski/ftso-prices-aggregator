package dev.mouradski.ftso.prices.client.tidex;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ApiResponse {
    private boolean success;
    private String message;
    private Map<String, MarketData> result;
}
