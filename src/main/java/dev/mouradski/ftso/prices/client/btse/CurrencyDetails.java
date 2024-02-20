package dev.mouradski.ftso.prices.client.btse;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class CurrencyDetails {
    private Double last;
    @JsonProperty("lowest_ask")
    private Double lowestAsk;
    @JsonProperty("highest_bid")
    private Double highestBid;
    @JsonProperty("percent_change")
    private Double percentChange;
    private Double volume;
    private Double high24hr;
    private Double low24hr;
}
