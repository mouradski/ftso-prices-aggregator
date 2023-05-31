package dev.mouradski.ftsopriceclient.client.bitmart;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Root {
    @JsonProperty("data")
    private List<TradeData> data;
    
    @JsonProperty("table")
    private String table;
}
