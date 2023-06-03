package dev.mouradski.ftso.trades.client.digifinex;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MarketInfo {
    @SerializedName("volume_precision")
    private Double volumePrecision;

    @SerializedName("price_precision")
    private Double pricePrecision;

    @SerializedName("market")
    private String market;

    @SerializedName("min_amount")
    private Double minAmount;

    @SerializedName("min_volume")
    private Double minVolume;
}
