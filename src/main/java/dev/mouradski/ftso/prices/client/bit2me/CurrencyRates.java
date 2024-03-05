package dev.mouradski.ftso.prices.client.bit2me;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class CurrencyRates {
    private Map<String, ExchangeRates> currencyRates;
}
