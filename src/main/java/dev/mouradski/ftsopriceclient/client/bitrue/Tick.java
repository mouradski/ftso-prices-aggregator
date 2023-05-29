package dev.mouradski.ftsopriceclient.client.bitrue;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Tick {
    private List<BitrueTrade> data;

    // Getters and setters...
}
