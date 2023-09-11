package dev.mouradski.ftso.trades.client.bitmart;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TickerResponse {
    private int code;
    private String message;
    private List<String[]> data;
    private String trace;

}
