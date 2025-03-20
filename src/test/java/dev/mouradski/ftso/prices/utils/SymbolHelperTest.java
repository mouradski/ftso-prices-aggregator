package dev.mouradski.ftso.prices.utils;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SymbolHelperTest {

    @ParameterizedTest
    @MethodSource("provideSymbolsForTesting")
    void getSymbol_withValidInput(String symbol, Pair<String, String> expected) {
        Pair<String, String> result = SymbolHelper.getPair(symbol);
        assertEquals(expected.getLeft(), result.getLeft());
        assertEquals(expected.getRight(), result.getRight());
    }

    private static Stream<Arguments> provideSymbolsForTesting() {
        return Stream.of(
                Arguments.of("btc_usdt", Pair.of("BTC", "USDT")),
                Arguments.of("eth-busd", Pair.of("ETH", "BUSD")),
                Arguments.of("ltc/usdc", Pair.of("LTC", "USDC")),
                Arguments.of("bnbUSDT", Pair.of("BNB", "USDT")),
                Arguments.of("usdcusdt", Pair.of("USDC", "USDT")),
                Arguments.of("usdtbusd", Pair.of("USDT", "BUSD")),
                Arguments.of("adafdusd", Pair.of("ADA", "FDUSD")),
                Arguments.of("ada/fdusd", Pair.of("ADA", "FDUSD")),
                Arguments.of("adausde", Pair.of("ADA", "USDE")),
                Arguments.of("ada/usde", Pair.of("ADA", "USDE")),
                Arguments.of("susdt", Pair.of("S", "USDT")),
                Arguments.of("s/usdt", Pair.of("S", "USDT")),
                Arguments.of("susdd", Pair.of("S", "USDD")),
                Arguments.of("sfdusd", Pair.of("S", "FDUSD"))
        );
    }
}
