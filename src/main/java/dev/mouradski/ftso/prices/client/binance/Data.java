package dev.mouradski.ftso.prices.client.binance;

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
    @JsonProperty("s")
    public String s;
    @JsonProperty("c")
    public Double c;
    @JsonProperty("p")
    public Double p;
    @JsonProperty("q")
    public Double q;
    @JsonProperty("T")
    public Long t;

}
