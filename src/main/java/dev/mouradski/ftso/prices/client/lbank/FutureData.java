package dev.mouradski.ftso.prices.client.lbank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class FutureData {

    private String symbol;
    private String lastPrice;
}
