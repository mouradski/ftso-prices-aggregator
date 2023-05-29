package dev.mouradski.ftsopriceclient.client.kucoin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "type",
        "topic",
        "subject",
        "data"
})
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class KucoinTrade {

    @JsonProperty("type")
    public String type;
    @JsonProperty("topic")
    public String topic;
    @JsonProperty("subject")
    public String subject;
    @JsonProperty("data")
    public Data data;

}
