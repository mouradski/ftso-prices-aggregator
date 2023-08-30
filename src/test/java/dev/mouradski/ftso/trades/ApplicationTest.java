package dev.mouradski.ftso.trades;

import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.server.TradeServer;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;

import static org.mockito.Mockito.timeout;

@QuarkusTest
@TestProfile(dev.mouradski.ftso.trades.TestProfile.class)
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
            Mockito.verify(tradeServer, timeout(60000).atLeast(1).description("No trades received from exchange " + exchange)).broadcast(Mockito.argThat((Trade trade) -> trade.getExchange().equals(exchange)));
        });
    }
}
