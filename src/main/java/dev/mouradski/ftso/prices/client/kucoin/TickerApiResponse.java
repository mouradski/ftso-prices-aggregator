package dev.mouradski.ftso.prices.client.kucoin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TickerApiResponse {
    private String code;
    private TickerData data;
}
