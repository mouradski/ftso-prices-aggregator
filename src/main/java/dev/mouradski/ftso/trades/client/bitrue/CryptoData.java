package dev.mouradski.ftso.trades.client.bitrue;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class CryptoData {
    private List<CryptoItem> items;

    @JsonProperty("data")
    public List<CryptoItem> getItems() {
        return items;
    }

    public void setItems(List<CryptoItem> items) {
        this.items = items;
    }
}

@Getter
@Setter
class CryptoItem {
    private String symbol;
    private String priceChange;
    private String priceChangePercent;
    private String weightedAvgPrice;
    private String prevClosePrice;
    private Double lastPrice;
    private String lastQty;
    private String bidPrice;
    private String askPrice;
    private String openPrice;
    private String highPrice;
    private String lowPrice;
    private String volume;
    private String quoteVolume;
    private long openTime;
    private long closeTime;
    private long firstId;
    private long lastId;
    private long count;

    // Getters and setters for all fields

    @JsonProperty("symbol")
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @JsonProperty("priceChange")
    public String getPriceChange() {
        return priceChange;
    }

    public void setPriceChange(String priceChange) {
        this.priceChange = priceChange;
    }

    @JsonProperty("priceChangePercent")
    public String getPriceChangePercent() {
        return priceChangePercent;
    }

    public void setPriceChangePercent(String priceChangePercent) {
        this.priceChangePercent = priceChangePercent;
    }

    @JsonProperty("weightedAvgPrice")
    public String getWeightedAvgPrice() {
        return weightedAvgPrice;
    }

    public void setWeightedAvgPrice(String weightedAvgPrice) {
        this.weightedAvgPrice = weightedAvgPrice;
    }

    // Continue with getters and setters for the remaining fields...
}
