package dev.mouradski.ftso.trades.client.xt;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventData {
    private String topic;
    private String event;
    private EventDetail data;
}
