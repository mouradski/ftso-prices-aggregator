package dev.mouradski.ftso.prices.client.bitso;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Market {
    private boolean success;
    private List<PayloadItem> payload;
}
