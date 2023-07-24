package dev.mouradski.ftso.trades.client.gateio;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GateIOTrade {
    private long id;
    private long createTime;
    @SerializedName("create_time_ms")
    private String createTimeMs;
    private String side;
    @SerializedName("currency_pair")
    private String currencyPair;
    private Double amount;
    private Double price;
}
