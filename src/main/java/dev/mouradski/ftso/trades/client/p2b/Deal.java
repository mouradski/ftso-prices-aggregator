package dev.mouradski.ftso.trades.client.p2b;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Deal {
    private long id;
    private double time;
    private String price;
    private String amount;
    private String type;
}
