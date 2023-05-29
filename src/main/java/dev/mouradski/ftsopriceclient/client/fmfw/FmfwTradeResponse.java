package dev.mouradski.ftsopriceclient.client.fmfw;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class FmfwTradeResponse {
    public String ch;
    public Update update;

    @Getter
    @Setter
    public static class Update {
        private Map<String, List<Trade>> trades = new HashMap<>();

        @JsonAnySetter
        public void setTrades(String name, List<Trade> trades) {
            this.trades.put(name, trades);
        }

        public Map<String, List<Trade>> getTrades() {
            return trades;
        }
    }

    @Getter
    @Setter
    public static class Trade {
        public Long t;
        public Long i;
        public Double p;
        public Double q;
        public String s;
    }
}
