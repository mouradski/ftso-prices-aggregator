package dev.mouradski.ftso.prices.client.fastex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class TickerData {

    @JsonProperty("last_price")
    private Double lastPrice;

    private String symbol;
}
