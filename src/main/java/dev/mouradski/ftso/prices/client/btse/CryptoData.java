package dev.mouradski.ftso.prices.client.btse;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class CryptoData {
    private Map<String, CurrencyDetails> data;
}
