package dev.mouradski.ftsopriceclient.client.bitmart;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SymbolResponse {
    private String message;
    private int code;
    private String trace;
    private SymbolData data;
}
