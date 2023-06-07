package dev.mouradski.ftso.trades.client.toobit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TradeMessage {

    @JsonProperty("symbol")
    private String symbolName;

    @JsonProperty("data")
    private List<ToobitTrade> trades;
}
