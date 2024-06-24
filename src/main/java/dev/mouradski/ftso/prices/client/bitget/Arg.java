package dev.mouradski.ftso.prices.client.bitget;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
class Arg {
    @JsonProperty("instType")
    private String instType;
    @JsonProperty("channel")
    private String channel;
    @JsonProperty("instId")
    private String instId;
}
