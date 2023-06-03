package dev.mouradski.ftso.trades.client.crypto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Response {
    @SerializedName("id")
    private int id;

    @SerializedName("code")
    private int code;

    @SerializedName("method")
    private String method;

    @SerializedName("result")
    private Result result;
}
