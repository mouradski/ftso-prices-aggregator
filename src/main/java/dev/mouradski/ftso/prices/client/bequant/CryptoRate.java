package dev.mouradski.ftso.prices.client.bequant;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
class CryptoRate {
    public String ch;
    @JsonProperty("target_currency")
    public String targetCurrency;
    public Map<String, CurrencyData> data;
}
