package dev.mouradski.ftsopriceclient.client.bittrex;


import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenResponse {
    @SerializedName("ConnectionToken")
    private String connectionToken;
}
