package dev.mouradski.ftso.trades;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;


import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.client.bitstamp.BitstampClientEndpoint;
import dev.mouradski.ftso.trades.client.coinbase.CoinbaseClientEndpoint;
import dev.mouradski.ftso.trades.client.coinbase.TradeMatch;
import dev.mouradski.ftso.trades.client.gateio.GateIOClientEndpoint;
import dev.mouradski.ftso.trades.client.gateio.GateIOTrade;
import dev.mouradski.ftso.trades.model.Ticker;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.server.TickerServer;
import dev.mouradski.ftso.trades.server.TradeServer;
import dev.mouradski.ftso.trades.service.TradeService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;

@QuarkusTest
@TestProfile(dev.mouradski.ftso.trades.TestProfile.class)
public class ApplicationTest {

    private final InMemoryLogHandler inMemoryLogHandler = new InMemoryLogHandler();

    @InjectMock
    TradeServer tradeServer;

    @InjectMock
    TickerServer tickerServer;

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

    @BeforeAll
    public static void setUp() {
        TradeServer tradeServerMock = Mockito.mock(TradeServer.class);
        QuarkusMock.installMockForType(tradeServerMock, TradeServer.class);

        TickerServer tickerServerMock = Mockito.mock(TickerServer.class);
        QuarkusMock.installMockForType(tickerServerMock, TickerServer.class);
    }

    @Test
    void shouldBroadcastTrades() {
        Arrays.asList("binance").forEach(exchange -> {
            Mockito.verify(tradeServer, timeout(60000).atLeast(1).description("No trades received from exchange " + exchange)).broadcast(Mockito.argThat((Trade trade) -> trade.getExchange().equals(exchange)));
        });
    }

    @Test
    void shouldBroadcastTickers() {
        Arrays.asList("binance").forEach(exchange -> {
            Mockito.verify(tickerServer, timeout(60000).atLeast(1).description("No tickers received from exchange " + exchange)).broadcast(Mockito.argThat((Ticker ticker) -> ticker.getExchange().equals(exchange)));
        });
    }



    @Test
    void shouldReconnect() throws JsonProcessingException, InterruptedException {
        AbstractClientEndpoint clientEndpoint = new CoinbaseClientEndpoint();

        clientEndpoint.setTradeService(tradeService);
        clientEndpoint.setTimeout(3);
        clientEndpoint.setExchanges(Arrays.asList("coinbase"));
        clientEndpoint.setAssets(Arrays.asList("btc"));
        clientEndpoint.setSubscribeTicker(true);
        clientEndpoint.setSubscribeTrade(true);


        var tradeMsg = new TradeMatch();
        tradeMsg.setSize(100d);
        tradeMsg.setPrice(1d);
        tradeMsg.setSide("sell");
        tradeMsg.setProductId("BTC_USDT");
        tradeMsg.setType("match");

        clientEndpoint.connect();

        Assertions.assertTrue(inMemoryLogHandler.getLogRecords().stream().anyMatch(l -> l.getMessage().equals("Connected to coinbase")));
        inMemoryLogHandler.getLogRecords().clear();
        Thread.sleep(5000);

        Assertions.assertTrue(inMemoryLogHandler.getLogRecords().stream().anyMatch(l -> l.getMessage().equals("No trade received from coinbase for 3 seconds. Reconnecting...")));
        Thread.sleep(1000);
        Assertions.assertTrue(inMemoryLogHandler.getLogRecords().stream().anyMatch(l -> l.getMessage().equals("Connected to coinbase")));
        inMemoryLogHandler.getLogRecords().clear();


        var executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> testWsServer.broadcast(tradeMsg), 2, TimeUnit.SECONDS);


        Thread.sleep(5000);

        Assertions.assertFalse(inMemoryLogHandler.getLogRecords().stream().anyMatch(l -> l.getMessage().equals("No trade received from coinbase for 3 seconds. Reconnecting...")));
    }
}
