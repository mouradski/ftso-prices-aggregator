package dev.mouradski.ftso.prices.client.bybit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "s",
        "p",
        "q"
})
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString
public class Data {
    @JsonProperty("v") // trade id
    public String v;
    @JsonProperty("t") // timestamp
    public Long t;
    @JsonProperty("p") // price
    public String p;
    @JsonProperty("q") // quantity
    public String q;

}
