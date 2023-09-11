package dev.mouradski.ftso.trades.client.gateio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SymbolData {
    private String id;
    private String base;
    private String quote;
}
