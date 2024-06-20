package dev.mouradski.ftso.prices.client.nami;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TickerResponse {
    private List<TickerData> data;
}
