package dev.mouradski.ftso.prices.client.bitunix;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tickers {
    private List<Ticker> data;
}
