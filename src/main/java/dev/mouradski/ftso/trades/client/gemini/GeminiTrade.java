package dev.mouradski.ftso.trades.client.gemini;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GeminiTrade {
    @JsonProperty("type")
    private String type;

    @JsonProperty("eventId")
    private long eventId;

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("timestampms")
    private long timestampMs;

    @JsonProperty("socket_sequence")
    private int socketSequence;

    @JsonProperty("events")
    private List<Event> events;

    @Getter
    @Setter
    public static class Event {
        @JsonProperty("type")
        private String eventType;

        @JsonProperty("tid")
        private long tradeId;

        @JsonProperty("price")
        private Double price;

        @JsonProperty("amount")
        private Double amount;

        @JsonProperty("makerSide")
        private String makerSide;

        @JsonProperty("symbol")
        private String symbol;

        @JsonProperty("side")
        private String side;

        @JsonProperty("remaining")
        private String remaining;

        @JsonProperty("reason")
        private String reason;

    }
}
