package dev.mouradski.ftso.trades.utils;

import org.apache.commons.lang3.tuple.Pair;

public class SymbolHelper {

    private SymbolHelper() {
    }

    public static Pair<String, String> getPair(String remotePair) {
        String pair = cleanRemotePair(remotePair);
        String baseCurrency = getBaseCurrency(pair);
        String quoteCurrency = pair.replace(baseCurrency, "");

        return Pair.of(baseCurrency, quoteCurrency);
    }

    private static String cleanRemotePair(String remotePair) {
        return remotePair.replace("-", "").replace("_", "").replace("/", "").toUpperCase();
    }

    private static String getBaseCurrency(String pair) {

        var baseCurrencies = new String[] {"USDT", "USDC", "BUSD"};

        for (var baseCurrency : baseCurrencies) {
            if (pair.startsWith(baseCurrency)) {
                return baseCurrency;
            }
        }

        var quote = pair.substring(pair.length() - 4);

        if (!quote.startsWith("U") && !quote.startsWith("B")) {
            quote = "USD";
        }

        var base = pair.replace(quote, "");

        if (base.length() == 2) {
            quote = "USD";
            base = pair.replace(quote, "");
        }

        return base;
    }
}
