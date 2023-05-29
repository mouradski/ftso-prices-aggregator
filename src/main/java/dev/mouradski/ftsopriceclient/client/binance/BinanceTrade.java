package dev.mouradski.ftsopriceclient.client.binance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "stream",
        "data"
})
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString
public class BinanceTrade {

    @JsonProperty("stream")
    public String stream;
    @JsonProperty("data")
    public Data data;

}
