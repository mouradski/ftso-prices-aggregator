package dev.mouradski.ftso.trades.client.bitrue;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BitrueTrade {
    private Long id;
    private Double price;
    private Double amount;
    private String side;
    private Double vol;
    private Long ts;
    private String ds;

    // Getters and setters...
}
