package dev.mouradski.ftso.trades;

import dev.mouradski.ftso.trades.model.Ticker;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.server.TickerServer;
import dev.mouradski.ftso.trades.server.TradeServer;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.timeout;

@QuarkusTest
@TestProfile(ApplicationTest.class)
public class ApplicationTest implements QuarkusTestProfile {

    @ConfigProperty(name = "exchanges")
    Set<String> exchanges;

    @InjectMock
    TradeServer tradeServer;

    @InjectMock
    TickerServer tickerServer;


    @BeforeAll
    public static void setUp() {
        TradeServer tradeServerMock = Mockito.mock(TradeServer.class);
        QuarkusMock.installMockForType(tradeServerMock, TradeServer.class);

        TickerServer tickerServerMock = Mockito.mock(TickerServer.class);
        QuarkusMock.installMockForType(tickerServerMock, TickerServer.class);
    }

    @Test
    void shouldBroadcastTrades() {
        exchanges.forEach(exchange -> {
            Mockito.verify(tradeServer, timeout(60000).atLeast(1).description("No trades received from exchange " + exchange)).broadcast(Mockito.argThat((Trade trade) -> trade.getExchange().equals(exchange)));
        });

    }

    @Test
    void shouldBroadcastTickers() {
        exchanges.forEach(exchange -> {
            Mockito.verify(tickerServer, timeout(60000).atLeast(1).description("No tickers received from exchange " + exchange)).broadcast(Mockito.argThat((Ticker ticker) -> ticker.getExchange().equals(exchange)));
        });

    }


    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("exchanges", "coinbase,bitrue,kraken,gateio", "subscribe.trade", "true", "subscribe.ticker", "true");
    }
}
