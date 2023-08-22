package dev.mouradski.ftso.trades.client.huobi;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TradeTick {
    @SerializedName("id")
    private long id;

    @SerializedName("ts")
    private long ts;

    @SerializedName("data")
    private List<TradeDetail> data;
}
