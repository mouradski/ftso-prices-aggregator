package dev.mouradski.ftso.prices.client.citex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class RestApiTicker {
    private List<RestTickerData> ticker;
}
