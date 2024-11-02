package dev.mouradski.ftso.prices.client.indoex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TickerData {
    private String pair;

    @JsonProperty("last")
    private String last;

}
