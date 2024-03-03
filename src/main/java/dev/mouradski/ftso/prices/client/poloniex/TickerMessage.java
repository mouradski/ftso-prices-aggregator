package dev.mouradski.ftso.prices.client.poloniex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TickerMessage {
    private String channel;
    private List<TickerData> data;
    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public List<TickerData> getData() {
        return data;
    }

    public void setData(List<TickerData> data) {
        this.data = data;
    }
}

@Getter
@Setter
class TickerData {
    private String symbol;
    private long startTime;
    private String open;
    private String high;
    private String low;
    private String close;
    private String quantity;
    private String amount;
    private int tradeCount;
    private String dailyChange;
    private String markPrice;
    @JsonProperty("closeTime")
    private long closeTime;
    private long ts;
}
