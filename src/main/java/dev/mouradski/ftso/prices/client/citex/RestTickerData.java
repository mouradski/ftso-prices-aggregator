package dev.mouradski.ftso.prices.client.citex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class RestTickerData {
    private String symbol;
    private String last;
}
