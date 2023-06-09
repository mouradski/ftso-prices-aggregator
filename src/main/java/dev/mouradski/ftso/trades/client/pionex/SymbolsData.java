package dev.mouradski.ftso.trades.client.pionex;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SymbolsData {
    private List<SymbolData> symbols;
}
