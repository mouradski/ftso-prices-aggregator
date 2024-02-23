package dev.mouradski.ftso.prices.client.ascendex;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class SummaryMessage {
    public String m; // message type
    public String s; // symbol
    public Data data;

    @Getter
    @Setter
    static class Data {
        public String i; // interval
        public long ts; // timestamp
        public String o; // open price
        public String c; // close price
        public String h; // high price
        public String l; // low price
        public String v; // volume
    }
}
