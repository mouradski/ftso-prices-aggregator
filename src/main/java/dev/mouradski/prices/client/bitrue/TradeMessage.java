package dev.mouradski.prices.client.bitrue;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TradeMessage {
    private String channel;
    private Tick tick;
    private Long ts;
}
