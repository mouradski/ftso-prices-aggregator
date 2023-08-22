package dev.mouradski.ftso.trades.client.huobi;


import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TradeResponse {
    @SerializedName("ch")
    private String ch;

    @SerializedName("ts")
    private long ts;

    @SerializedName("tick")
    private TradeTick tick;
}
