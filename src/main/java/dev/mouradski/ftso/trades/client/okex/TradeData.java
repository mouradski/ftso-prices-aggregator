package dev.mouradski.ftso.trades.client.okex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TradeData {

    private Arg arg;
    private List<Data> data;

    @Getter
    @Setter
    public static class Arg {
        private String channel;
        @JsonProperty("instId")
        private String instId;

    }

    @Getter
    @Setter
    public static class Data {
        @JsonProperty("instId")
        private String instId;
        @JsonProperty("tradeId")
        private String tradeId;
        private Double px;
        private Double sz;
        private String side;
        private String ts;
    }
}
