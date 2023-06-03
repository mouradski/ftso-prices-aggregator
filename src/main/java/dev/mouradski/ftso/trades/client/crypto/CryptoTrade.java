package dev.mouradski.ftso.trades.client.crypto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CryptoTrade {
    @SerializedName("d")
    private String d;

    @SerializedName("t")
    private long t;

    @SerializedName("p")
    private double p;

    @SerializedName("q")
    private double q;

    @SerializedName("s")
    private String s;

    @SerializedName("i")
    private String i;
}
