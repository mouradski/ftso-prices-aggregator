package dev.mouradski.prices.client.bitget;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Update {
    private String action;
    private Arg arg;
    private List<TradeUpdateData> data;
}
