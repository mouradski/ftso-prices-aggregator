package dev.mouradski.ftso.prices.client.bitmart;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class TickerData {
    @JsonProperty("base_volume_24h")
    public String baseVolume24h;
    @JsonProperty("high_24h")
    public String high24h;
    @JsonProperty("last_price")
    public String lastPrice;
    @JsonProperty("low_24h")
    public String low24h;
    @JsonProperty("open_24h")
    public String open24h;
    @JsonProperty("s_t")
    public long sT;
    public String symbol;
}
