package dev.mouradski.ftso.trades.client.hitbtc;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HitbtcTrade {
    @SerializedName("t")
    private long t;

    @SerializedName("i")
    private long i;

    @SerializedName("p")
    private Double p;

    @SerializedName("q")
    private Double q;

    @SerializedName("s")
    private String s;
}
