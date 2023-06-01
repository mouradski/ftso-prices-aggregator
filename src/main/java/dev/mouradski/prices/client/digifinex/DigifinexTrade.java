package dev.mouradski.prices.client.digifinex;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DigifinexTrade {
    private Object id;
    private Object time;
    private Double amount;
    private Double price;
    private String type;
}
