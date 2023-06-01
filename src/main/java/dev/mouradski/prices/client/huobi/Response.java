package dev.mouradski.prices.client.huobi;


import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Response {
    @SerializedName("ch")
    private String ch;

    @SerializedName("ts")
    private long ts;

    @SerializedName("tick")
    private Tick tick;
}
