package dev.mouradski.ftso.prices.utils;

import org.apache.commons.lang3.tuple.Pair;

public class SymbolHelper {

    private SymbolHelper() {
    }

    public static Pair<String, String> getPair(String remotePair) {
        String pair = cleanRemotePair(remotePair);
        String baseCurrency = getBaseCurrency(pair);
        String quoteCurrency = pair.replaceFirst("^" + baseCurrency, "");

        return Pair.of(baseCurrency, quoteCurrency);
    }

    private static String cleanRemotePair(String remotePair) {
        return remotePair.replace("-", "").replace("_", "").replace("/", "").replace(":", "").toUpperCase();
    }

    
    private static String getBaseCurrency(String pair) {

        var baseCurrencies = new String[] {"USDT", "USDC", "BUSD"};

        for (var baseCurrency : baseCurrencies) {
            if (pair.startsWith(baseCurrency)) {
                return baseCurrency;
            }
        }

        if (pair.endsWith("FDUSD")) {
            return pair.replace("FDUSD", "");
        }

        if (pair.endsWith("USDE")) {
            return pair.replace("USDE", "");
        }


        var quote = pair.endsWith("DAI") ? "DAI" : pair.substring(pair.length() - 4);

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
