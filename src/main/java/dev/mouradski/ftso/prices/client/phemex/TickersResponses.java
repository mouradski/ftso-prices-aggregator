package dev.mouradski.ftso.prices.client.phemex;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TickersResponses {
    private String error;

    private Integer id;

    private List<TickerData> result;


}
