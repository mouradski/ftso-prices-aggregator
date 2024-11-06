package dev.mouradski.ftso.prices.client.bitopro;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tickers {
    private List<TickerData> data;
}
