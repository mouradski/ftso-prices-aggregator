package dev.mouradski.prices.client.mexc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Data {
    @JsonProperty("deals")
    private List<Deal> deals;
}
