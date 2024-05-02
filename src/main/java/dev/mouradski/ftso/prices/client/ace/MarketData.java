package dev.mouradski.ftso.prices.client.ace;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class MarketData {
    @JsonProperty("base_volume")
    private String baseVolume;
    private String changeRate;
    @JsonProperty("last_price")
    private String lastPrice;
    @JsonProperty("quote_volume")

    private String quoteVolume;

}
