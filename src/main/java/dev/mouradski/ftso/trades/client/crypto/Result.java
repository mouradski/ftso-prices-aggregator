package dev.mouradski.ftso.trades.client.crypto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Result {
    @SerializedName("channel")
    private String channel;

    @SerializedName("subscription")
    private String subscription;

    @SerializedName("instrument_name")
    private String instrumentName;

    @SerializedName("data")
    private List<CryptoTrade> data;
}
