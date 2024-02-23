package dev.mouradski.ftso.prices.client.exmo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class TickerData {
    public String buy_price;
    public String sell_price;
    public String last_trade;
    public String high;
    public String low;
    public String avg;
    public String vol;
    public String vol_curr;
    public long updated;
}
