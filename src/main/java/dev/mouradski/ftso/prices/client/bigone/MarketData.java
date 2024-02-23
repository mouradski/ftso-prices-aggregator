package dev.mouradski.ftso.prices.client.bigone;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class MarketData {
    @JsonProperty("asset_pair_name")
    public String assetPairName;
    public PriceDetail bid;
    public PriceDetail ask;
    public String open;
    public String high;
    public String low;
    public String close;
    public String volume;
    @JsonProperty("daily_change")
    public String dailyChange;
}
