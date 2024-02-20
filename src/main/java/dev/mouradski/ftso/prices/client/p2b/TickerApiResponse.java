package dev.mouradski.ftso.prices.client.p2b;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class TickerApiResponse {
    private boolean success;
    private String errorCode;
    private String message;
    private Map<String, CurrencyData> result;
    @JsonProperty("cache_time")
    private double cacheTime;
    @JsonProperty("current_time")
    private double currentTime;
}
