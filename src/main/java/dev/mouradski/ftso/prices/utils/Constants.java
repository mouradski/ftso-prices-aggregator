package dev.mouradski.ftso.prices.utils;

import java.util.List;

public class Constants {

    private Constants() {
    }
    public static final String USD = "usd";
    public static final String USDT = "usdt";
    public static final String USDC = "usdc";
    public static final String DAI = "dai";

    public static final List<String> USD_USDT_USDC_DAI = List.of(USD, USDT, USDC, DAI, "fdusd", "tusd", "usdd");
    public static final List<String> USDT_USDC_DAI = List.of(USDT, USDC, DAI);

    public static final List<String> SYMBOLS = List.of("xrp", "btc", "eth", "algo", "xlm", "ada", "matic", "sol", "fil", "flr", "sgb", "doge", "xdc", "arb", "avax", "bnb", "usdc", "busd", "usdt");
}
