package dev.mouradski.ftso.prices.client.bigone;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class MarketData {
    public String asset_pair_name;
    public PriceDetail bid;
    public PriceDetail ask;
    public String open;
    public String high;
    public String low;
    public String close;
    public String volume;
    public String daily_change;
}
