package dev.mouradski.ftso.prices.client.bit2me;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ExchangeRates {
    private Map<String, Double> rates;
}
