package dev.mouradski.ftso.trades.client.p2b;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
class DealsResponse {
    private String method;
    private List<Object> params;
    @JsonProperty("id")
    private Object idValue;
}
