package dev.mouradski.ftso.trades.client.toobit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ToobitTrade {

    @JsonProperty("p")
    private Double price;

    @JsonProperty("q")
    private Double quantity;

    @JsonProperty("t")
    private Long time;
}
