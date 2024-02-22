package dev.mouradski.ftso.trades.client.tapbit;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TickerDataWrapper {
    private String topic;
    private List<TickerData> data;


    public List<TickerData> getData() {
        return data;
    }

    public void setData(List<TickerData> data) {
        this.data = data;
    }
}
