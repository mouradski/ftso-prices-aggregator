package dev.mouradski.ftso.prices.utils;

import java.util.List;

public class Constants {

    private Constants() {
    }
    public static final String USD = "usd";
    public static final String USDT = "usdt";
    public static final String USDC = "usdc";
    public static final String DAI = "dai";
    public static final String USDE = "usde";
    public static final String FDUSD = "fdusd";
    public static final String USDD = "usdd";
    public static final String TUSD = "tusd";



    public static final List<String> ALL_QUOTES = List.of(USD, USDT, USDC, DAI, FDUSD, TUSD, USDD, USDE);
    public static final List<String> USDT_USDC_DAI = List.of(USDT, USDC, DAI);

    public static final List<String> SYMBOLS = List.of("xrp", "btc", "eth", "algo", "xlm", "ada", "matic", "sol", "fil", "flr", "sgb", "doge", "xdc", "arb", "avax", "bnb", "usdc", "busd", "usdt");
}
