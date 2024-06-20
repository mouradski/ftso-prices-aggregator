package dev.mouradski.ftso.prices.client.websea;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Result {
    private String symbol;
    private Market data;
}
