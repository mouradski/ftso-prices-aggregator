package dev.mouradski.ftso.prices.client.bitmart;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
class SpotTicker {
    public List<TickerData> data;
    public String table;
}
