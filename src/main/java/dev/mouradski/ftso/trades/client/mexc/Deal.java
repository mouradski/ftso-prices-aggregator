package dev.mouradski.ftso.trades.client.mexc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Deal {
    @JsonProperty("t")
    private Long t;

    @JsonProperty("p")
    private Double p;

    @JsonProperty("q")
    private Double q;

    @JsonProperty("T")
    private Long t2;

    @JsonProperty("M")
    private Long m;
}
