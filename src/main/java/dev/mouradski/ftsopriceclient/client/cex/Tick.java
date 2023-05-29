package dev.mouradski.ftsopriceclient.client.cex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Tick {

    @JsonProperty("e")
    private String e;

    @JsonProperty("data")
    private TickData data;
}
