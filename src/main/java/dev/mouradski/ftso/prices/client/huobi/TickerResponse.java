package dev.mouradski.ftso.prices.client.huobi;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TickerResponse {
    @SerializedName("ch")
    private String ch;

    @SerializedName("ts")
    private long ts;

    @SerializedName("tick")
    private TickerDetail tick;
}
