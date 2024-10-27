package dev.mouradski.ftso.prices.client.probit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketData {
    private String channel;

    @JsonProperty("market_id")
    private String marketId;

    private PriceData ticker;
}
