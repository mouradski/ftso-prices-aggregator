package dev.mouradski.ftso.prices.client.blofin;

import lombok.Getter;

import java.util.List;

@Getter
public class MarketData {
    private Arg arg;
    private List<DataItem> data;
}
