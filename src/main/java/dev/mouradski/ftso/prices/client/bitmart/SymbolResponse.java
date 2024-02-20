package dev.mouradski.ftso.prices.client.bitmart;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SymbolResponse {
    private String message;
    private int code;
    private String trace;
    private SymbolData data;
}
