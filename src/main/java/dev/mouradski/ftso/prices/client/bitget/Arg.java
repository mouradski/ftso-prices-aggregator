package dev.mouradski.ftso.prices.client.bitget;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class Arg {
    @JsonProperty("instType")
    private String instType;
    @JsonProperty("channel")
    private String channel;
    @JsonProperty("instId")
    private String instId;
}
