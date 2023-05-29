package dev.mouradski.ftsopriceclient.client.digifinex;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DigifinexTrade {
    private long id;
    private long time;
    private Double amount;
    private Double price;
    private String type;
}
