package dev.mouradski.prices.utils;


import org.apache.commons.lang3.tuple.Pair;

public class SymbolHelper {

    public static Pair<String, String> getSymbol(String remotePair) {

        String pair = remotePair.replace("-", "").replace("_", "").replace("/", "").toUpperCase();

        if (pair.startsWith("USDT")) {
             return Pair.of("USDT", pair.replace("USDT", ""));
        } else if (pair.startsWith("USDC")) {
            return Pair.of("USDC", pair.replace("USDC", ""));
        } else if (pair.startsWith("BUSD")) {
            return Pair.of("BUSD", pair.replace("BUSD", ""));
        }

        String quote = pair.substring(pair.length() - 4);

        if (!quote.startsWith("U") && !quote.startsWith("B")) {
            quote = "USD";
        }

        return Pair.of(pair.replace(quote, ""), quote);
    }
}
