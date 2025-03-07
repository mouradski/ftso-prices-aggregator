package dev.mouradski.ftso.prices.client.emirex;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SymbolInfo {
    private boolean status;
    private List<Symbols> data;
}

@Getter
@Setter
class Symbols {
    private String id;
    private String pair;
    private String base;
    private String quote;
    private String rate_decimal;
    private String base_decimal;
    private String quote_decimal;

}
