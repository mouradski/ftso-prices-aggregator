package dev.mouradski.prices.client.digifinex;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TradeResponse {
    private String method;
    private List<Object> params;
}
