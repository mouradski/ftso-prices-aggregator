package dev.mouradski.ftso.prices.client.mexc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ContractData {
    private String symbol;
    private Double lastPrice;
}
