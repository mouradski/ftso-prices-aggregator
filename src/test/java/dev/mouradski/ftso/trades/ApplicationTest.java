package dev.mouradski.ftso.trades;

import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.server.TradeServer;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.Set;

import static org.mockito.Mockito.timeout;

@QuarkusTest
public class ApplicationTest {

    @ConfigProperty(name = "exchanges")
    Set<String> exchanges;

    @InjectMock
    TradeServer tradeServer;

    @BeforeAll
    public static void setUp() {
        TradeServer mock = Mockito.mock(TradeServer.class);
        QuarkusMock.installMockForType(mock, TradeServer.class);
    }

    @Test
    void shouldBroadcastTrades() {
        exchanges.forEach(exchange -> {
            Mockito.verify(tradeServer, timeout(60000).atLeast(1).description("No trades received from exchange " + exchange)).broadcastTrade(Mockito.argThat((Trade trade) -> trade.getExchange().equals(exchange)));
        });
    }
}
