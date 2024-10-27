package dev.mouradski.ftso.prices.client.luno;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tickers {
    private List<TickerData> tickers;
}
