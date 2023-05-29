package dev.mouradski.ftsopriceclient.client.liquid;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "product_type",
        "code",
        "name",
        "market_ask",
        "market_bid",
        "indicator",
        "currency",
        "currency_pair_code",
        "symbol",
        "btc_minimum_withdraw",
        "fiat_minimum_withdraw",
        "pusher_channel",
        "taker_fee",
        "maker_fee",
        "low_market_bid",
        "high_market_ask",
        "volume_24h",
        "last_price_24h",
        "last_traded_price",
        "last_traded_quantity",
        "average_price",
        "quoted_currency",
        "base_currency",
        "tick_size",
        "disabled",
        "margin_enabled",
        "cfd_enabled",
        "perpetual_enabled",
        "last_event_timestamp",
        "timestamp",
        "multiplier_up",
        "multiplier_down",
        "average_time_interval",
        "progressive_tier_eligible",
        "matching_state"
})
@Getter
@Setter
public class LiquidProduit {

    @JsonProperty("id")
    public String id;
    @JsonProperty("product_type")
    public String productType;
    @JsonProperty("code")
    public String code;
    @JsonProperty("name")
    public Object name;
    @JsonProperty("market_ask")
    public Integer marketAsk;
    @JsonProperty("market_bid")
    public Integer marketBid;
    @JsonProperty("indicator")
    public Object indicator;
    @JsonProperty("currency")
    public String currency;
    @JsonProperty("currency_pair_code")
    public String currencyPairCode;
    @JsonProperty("symbol")
    public Object symbol;
    @JsonProperty("btc_minimum_withdraw")
    public Object btcMinimumWithdraw;
    @JsonProperty("fiat_minimum_withdraw")
    public Object fiatMinimumWithdraw;
    @JsonProperty("pusher_channel")
    public String pusherChannel;
    @JsonProperty("taker_fee")
    public String takerFee;
    @JsonProperty("maker_fee")
    public String makerFee;
    @JsonProperty("low_market_bid")
    public String lowMarketBid;
    @JsonProperty("high_market_ask")
    public String highMarketAsk;
    @JsonProperty("volume_24h")
    public String volume24h;
    @JsonProperty("last_price_24h")
    public String lastPrice24h;
    @JsonProperty("last_traded_price")
    public String lastTradedPrice;
    @JsonProperty("last_traded_quantity")
    public String lastTradedQuantity;
    @JsonProperty("average_price")
    public String averagePrice;
    @JsonProperty("quoted_currency")
    public String quotedCurrency;
    @JsonProperty("base_currency")
    public String baseCurrency;
    @JsonProperty("tick_size")
    public String tickSize;
    @JsonProperty("disabled")
    public Boolean disabled;
    @JsonProperty("margin_enabled")
    public Boolean marginEnabled;
    @JsonProperty("cfd_enabled")
    public Boolean cfdEnabled;
    @JsonProperty("perpetual_enabled")
    public Boolean perpetualEnabled;
    @JsonProperty("last_event_timestamp")
    public String lastEventTimestamp;
    @JsonProperty("timestamp")
    public String timestamp;
    @JsonProperty("multiplier_up")
    public String multiplierUp;
    @JsonProperty("multiplier_down")
    public String multiplierDown;
    @JsonProperty("average_time_interval")
    public Integer averageTimeInterval;
    @JsonProperty("progressive_tier_eligible")
    public Boolean progressiveTierEligible;
    @JsonProperty("matching_state")
    public String matchingState;

}
