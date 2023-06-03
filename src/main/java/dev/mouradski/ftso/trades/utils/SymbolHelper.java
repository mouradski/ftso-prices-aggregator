package dev.mouradski.ftso.trades.utils;

import org.apache.commons.lang3.tuple.Pair;

public class SymbolHelper {

    public static Pair<String, String> getSymbol(String remotePair) {
        String pair = cleanRemotePair(remotePair);
        String baseCurrency = getBaseCurrency(pair);
        String quoteCurrency = pair.replace(baseCurrency, "");

        return Pair.of(baseCurrency, quoteCurrency);
    }

    private static String cleanRemotePair(String remotePair) {
        return remotePair.replace("-", "").replace("_", "").replace("/", "").toUpperCase();
    }

    private static String getBaseCurrency(String pair) {
        String[] baseCurrencies = {"USDT", "USDC", "BUSD"};

        for (String baseCurrency : baseCurrencies) {
            if (pair.startsWith(baseCurrency)) {
                return baseCurrency;
            }
        }

        String quote = pair.substring(pair.length() - 4);

        if (!quote.startsWith("U") && !quote.startsWith("B")) {
            quote = "USD";
        }

        return pair.replace(quote, "");
    }
}
