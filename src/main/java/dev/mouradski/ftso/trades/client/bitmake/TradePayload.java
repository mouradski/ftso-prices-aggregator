package dev.mouradski.ftso.trades.client.bitmake;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TradePayload {
    private String tp;
    private String e;
    private ProductSymbol ps;
    private List<BitmakeTrade> d;
    private boolean f;
}
