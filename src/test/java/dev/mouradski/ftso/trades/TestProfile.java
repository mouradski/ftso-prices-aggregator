package dev.mouradski.ftso.trades;

import io.quarkus.test.junit.QuarkusTestProfile;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;
import java.util.Optional;

public class TestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        Config config = ConfigProvider.getConfig();
        Optional<String> httpPortProperty = config.getOptionalValue("quarkus.http.test-port", String.class);

        return Map.of("exchanges", "coinbase", "coinbase.ws.uri", "ws://127.0.0.1:" + httpPortProperty.orElse("8081") + "/test", "subscribe.trade", "true", "subscribe.ticker", "true");
    }
}
