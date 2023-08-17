package dev.mouradski.ftso.trades;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class TestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("exchanges", "binance,kraken,bitrue,gateio,huobi");
    }
}
