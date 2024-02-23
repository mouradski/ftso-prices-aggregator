package dev.mouradski.ftso.prices.client.exmo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class TickerUpdate {
    public long ts;
    public String event;
    public String topic;
    public TickerData data;
}
