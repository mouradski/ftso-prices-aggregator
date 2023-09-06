package dev.mouradski.ftso.trades;

import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.client.coinbase.CoinbaseClientEndpoint;
import dev.mouradski.ftso.trades.client.coinbase.TradeMatch;
import dev.mouradski.ftso.trades.service.TradeService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@QuarkusTest
@TestProfile(ReconnectTest.class)
public class ReconnectTest implements QuarkusTestProfile {

    public ReconnectTest() {
    }
    private final InMemoryLogHandler inMemoryLogHandler = new InMemoryLogHandler();

    @Inject
    TestWsServer testWsServer;

    @Inject
    TradeService tradeService;

    @BeforeEach
    public void setUpLogHandler() {
        Logger logger = Logger.getLogger("");
        logger.addHandler(inMemoryLogHandler);
    }

    @AfterEach
    public void tearDown() {
        Logger logger = Logger.getLogger("");
        logger.removeHandler(inMemoryLogHandler);
    }

    @Test
    void shouldReconnectAfterServerDisconnect() throws InterruptedException {
        var clientEndpoint = configureEndpoint();
        clientEndpoint.connect();

        var executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> testWsServer.broadcast(getTrade()), 0, 2, TimeUnit.SECONDS);

        Thread.sleep(5000);

        Assertions.assertFalse(inMemoryLogHandler.getLogRecords().stream().anyMatch(l -> l.getMessage().equals("Closing websocket for coinbase, Reason :")));

        inMemoryLogHandler.getLogRecords().clear();

        testWsServer.disconnect();

        Thread.sleep(2000);

        Assertions.assertTrue(inMemoryLogHandler.getLogRecords().stream().anyMatch(l -> l.getMessage().equals("Closing websocket for coinbase, Reason : ")));

        inMemoryLogHandler.getLogRecords().clear();

        Thread.sleep(12000);

        Assertions.assertTrue(inMemoryLogHandler.getLogRecords().stream().anyMatch(l -> l.getMessage().equals("Connected to coinbase")));

        Thread.sleep(15000);
        Assertions.assertFalse(inMemoryLogHandler.getLogRecords().stream().anyMatch(l -> l.getMessage().equals("Closing websocket for coinbase, Reason : ")));


        inMemoryLogHandler.getLogRecords().clear();

    }
    @Test
    void shouldReconnectWhenNotReceivingMsg() throws InterruptedException, IOException {

        var clientEndpoint = configureEndpoint();

        clientEndpoint.connect();

        Assertions.assertTrue(inMemoryLogHandler.getLogRecords().stream().anyMatch(l -> l.getMessage().equals("Connected to coinbase")));
        inMemoryLogHandler.getLogRecords().clear();
        Thread.sleep(5000);

        Assertions.assertTrue(inMemoryLogHandler.getLogRecords().stream().anyMatch(l -> l.getMessage().equals("No trade received from coinbase for 3 seconds. Reconnecting...")));

        Thread.sleep(6000);
        Assertions.assertTrue(inMemoryLogHandler.getLogRecords().stream().anyMatch(l -> l.getMessage().equals("Opening websocket for coinbase ....")));
        Assertions.assertTrue(inMemoryLogHandler.getLogRecords().stream().anyMatch(l -> l.getMessage().equals("Connected to coinbase")));

        testWsServer.broadcast(getTrade());
        var executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> testWsServer.broadcast(getTrade()), 0, 2, TimeUnit.SECONDS);
        inMemoryLogHandler.getLogRecords().clear();

        Thread.sleep(12000);

        Assertions.assertFalse(inMemoryLogHandler.getLogRecords().stream().anyMatch(l -> l.getMessage().equals("No trade received from coinbase for 3 seconds. Reconnecting...")));

        clientEndpoint.shutdown();
    }

    @Test
    void shouldNotReconnect() throws  InterruptedException {

        var clientEndpoint = configureEndpoint();

        clientEndpoint.connect();

        var executor = Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(() -> testWsServer.broadcast(getTrade()), 0, 2, TimeUnit.SECONDS);
        Thread.sleep(12000);

        Assertions.assertFalse(inMemoryLogHandler.getLogRecords().stream().anyMatch(l -> l.getMessage().equals("No trade received from coinbase for 3 seconds. Reconnecting...")));
    }

    private TradeMatch getTrade() {
        var tradeMsg = new TradeMatch();
        tradeMsg.setSize(100d);
        tradeMsg.setPrice(1d);
        tradeMsg.setSide("sell");
        tradeMsg.setProductId("BTC_USDT");
        tradeMsg.setType("match");

        return tradeMsg;
    }

    private AbstractClientEndpoint configureEndpoint() {
        AbstractClientEndpoint clientEndpoint = new CoinbaseClientEndpoint();

        clientEndpoint.setTradeService(tradeService);
        clientEndpoint.setTimeout(3);
        clientEndpoint.setExchanges(Arrays.asList("coinbase"));
        clientEndpoint.setAssets(Arrays.asList("btc"));
        clientEndpoint.setSubscribeTicker(false);
        clientEndpoint.setSubscribeTrade(true);

        return clientEndpoint;
    }

    @Override
    public Map<String, String> getConfigOverrides() {
        Config config = ConfigProvider.getConfig();
        Optional<String> httpPortProperty = config.getOptionalValue("quarkus.http.test-port", String.class);
        return Map.of("exchanges", "coinbase", "coinbase.ws.uri", "ws://127.0.0.1:" + httpPortProperty.orElse("8081") + "/test");
    }

}
