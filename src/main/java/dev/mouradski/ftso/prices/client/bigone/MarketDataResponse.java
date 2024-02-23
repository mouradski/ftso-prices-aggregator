package dev.mouradski.ftso.prices.client.bigone;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
class MarketDataResponse {
    public int code;
    public List<MarketData> data;
}
