package dev.mouradski.ftso.prices.client.upbit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpbitTicker {
    private String code;

    @JsonProperty("trade_price")
    private Double lastPrice;
}
