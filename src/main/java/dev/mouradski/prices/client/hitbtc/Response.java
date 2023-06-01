package dev.mouradski.prices.client.hitbtc;


import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Response {
    @SerializedName("ch")
    private String ch;

    @SerializedName("snapshot")
    private Map<String, List<dev.mouradski.prices.client.hitbtc.HitbtcTrade>> snapshot = new HashMap<>();

    @SerializedName("update")
    private Map<String, List<HitbtcTrade>> update = new HashMap<>();
}
