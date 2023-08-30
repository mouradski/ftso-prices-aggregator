package dev.mouradski.ftso.trades.client.bitmart;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TickerResponse {

    private TickerData[] data;
    private String table;


    @Getter
    @Setter
    public static class TickerData {
        
        @JsonProperty("base_volume_24h")
        private String baseVolume24h;
        
        @JsonProperty("high_24h")
        private String high24h;
        
        @JsonProperty("last_price")
        private Double lastPrice;
        
        @JsonProperty("low_24h")
        private String low24h;
        
        @JsonProperty("open_24h")
        private String open24h;
        
        @JsonProperty("s_t")
        private long st;
        
        private String symbol;
    }
}
