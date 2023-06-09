package dev.mouradski.ftso.trades.client.pionex;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SymbolsResponse {
    private boolean result;
    private SymbolsData data;
}
