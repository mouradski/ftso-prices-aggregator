package dev.mouradski.ftso.prices.client.pionex;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SymbolsData {
    private List<SymbolData> symbols;
}
