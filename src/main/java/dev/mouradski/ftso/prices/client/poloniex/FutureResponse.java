package dev.mouradski.ftso.prices.client.poloniex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class FutureResponse {
    private FutureData data;
}
