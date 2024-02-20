package dev.mouradski.ftso.prices.client.xt;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TickerEvent {

    private String topic;
    private String event;
    private List<TickerData> data;

    @Getter
    @Setter
    public static class TickerData {

        @JsonProperty("s")
        private String symbol;

        @JsonProperty("t")
        private long timestamp;

        @JsonProperty("cv")
        private String cv;

        @JsonProperty("cr")
        private String cr;

        @JsonProperty("o")
        private String open;

        @JsonProperty("c")
        private String close;

        @JsonProperty("h")
        private String high;

        @JsonProperty("l")
        private String low;

        @JsonProperty("q")
        private String q;

        @JsonProperty("v")
        private String volume;

        // Getters, Setters, and Constructors...
    }
}
