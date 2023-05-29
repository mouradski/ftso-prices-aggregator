package dev.mouradski.ftsopriceclient.client.kucoin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "makerOrderId",
        "price",
        "sequence",
        "side",
        "size",
        "symbol",
        "takerOrderId",
        "time",
        "tradeId",
        "type"
})
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Data {

    @JsonProperty("makerOrderId")
    public String makerOrderId;
    @JsonProperty("price")
    public Double price;
    @JsonProperty("sequence")
    public String sequence;
    @JsonProperty("side")
    public String side;
    @JsonProperty("size")
    public Double size;
    @JsonProperty("symbol")
    public String symbol;
    @JsonProperty("takerOrderId")
    public String takerOrderId;
    @JsonProperty("time")
    public String time;
    @JsonProperty("tradeId")
    public String tradeId;
    @JsonProperty("type")
    public String type;

}
