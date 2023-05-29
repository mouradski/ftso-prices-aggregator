package dev.mouradski.ftsopriceclient.client.digifinex;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class MarketData {
    @SerializedName("data")
    private List<MarketInfo> data;
}
