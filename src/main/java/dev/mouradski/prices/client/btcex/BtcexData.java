package dev.mouradski.prices.client.btcex;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BtcexData {
    private Double price;
    private String index_name;
    private Long timestamp;
}
