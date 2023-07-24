package dev.mouradski.ftso.trades.client.bybit;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "topic",
        "params",
        "data"
})
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString
public class BybitTrade {
    @JsonProperty
    public String topic;
    @JsonProperty("params")
    public Params params;
    @JsonProperty("data")
    public Data data;

}