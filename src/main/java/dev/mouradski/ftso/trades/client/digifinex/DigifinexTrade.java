package dev.mouradski.ftso.trades.client.digifinex;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DigifinexTrade {
    private Object id;
    private Double time;
    private Double amount;
    private Double price;
    private String type;
}
