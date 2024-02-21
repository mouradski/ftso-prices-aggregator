package dev.mouradski.ftso.trades.client.biconomy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceUpdate {
    //{"method": "price.update", "params": ["BTC_USDT", "52347.17"], "id": null}
    private String[] params;
}
