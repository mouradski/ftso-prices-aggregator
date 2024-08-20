package dev.mouradski.ftso.prices.client.bitmart;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestSpotTickers {

    private List<List<String>> data;

}
