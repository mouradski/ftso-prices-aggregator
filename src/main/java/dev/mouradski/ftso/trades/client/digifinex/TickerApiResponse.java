package dev.mouradski.ftso.trades.client.digifinex;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TickerApiResponse {
    private List<TickerData> ticker;
    private long date;
    private int code;
}
