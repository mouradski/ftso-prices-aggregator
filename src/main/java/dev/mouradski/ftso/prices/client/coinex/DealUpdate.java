package dev.mouradski.ftso.prices.client.coinex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DealUpdate {
    private String method;
    private List<Object> params;
    private String id;

    @JsonProperty("params")
    private void unpackNested(List<Object> params) {
        this.params = params;
    }
}
