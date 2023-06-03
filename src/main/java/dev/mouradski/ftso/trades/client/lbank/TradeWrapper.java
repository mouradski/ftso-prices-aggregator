package dev.mouradski.ftso.trades.client.lbank;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TradeWrapper {

    @JsonProperty("trade")
    private Trade trade;

    @JsonProperty("SERVER")
    private String server;

    @JsonProperty("type")
    private String type;

    @JsonProperty("pair")
    private String pair;

    @JsonProperty("TS")
    private String ts;
}





