package dev.mouradski.ftso.trades;

import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.server.TradeServer;
import dev.mouradski.ftso.trades.service.TradeService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;

import static org.mockito.Mockito.timeout;

@QuarkusTest
public class ApplicationTest {

    @ConfigProperty(name = "exchanges")
    Set<String> exchanges;

    @Inject
    TradeService tradeService;

    @InjectMock
    TradeServer tradeServer;

    @Test
    void shouldBroadcastTrades() {
        tradeService.setTradeServer(tradeServer);
		// For a reason I can't explain, exchanes.forEach(exchange -> Mockito.verify) doesn't work
        Mockito.verify(tradeServer, timeout(30000).atLeast(1)).broadcastTrade(Mockito.argThat((Trade trade) -> trade.getExchange().equals("binance")));
        Mockito.verify(tradeServer, timeout(30000).atLeast(1)).broadcastTrade(Mockito.argThat((Trade trade) -> trade.getExchange().equals("okex")));
        Mockito.verify(tradeServer, timeout(30000).atLeast(1)).broadcastTrade(Mockito.argThat((Trade trade) -> trade.getExchange().equals("bitrue")));
        Mockito.verify(tradeServer, timeout(30000).atLeast(1)).broadcastTrade(Mockito.argThat((Trade trade) -> trade.getExchange().equals("whitebit")));
        Mockito.verify(tradeServer, timeout(30000).atLeast(1)).broadcastTrade(Mockito.argThat((Trade trade) -> trade.getExchange().equals("bingx")));
        Mockito.verify(tradeServer, timeout(30000).atLeast(1)).broadcastTrade(Mockito.argThat((Trade trade) -> trade.getExchange().equals("kucoin")));
        Mockito.verify(tradeServer, timeout(30000).atLeast(1)).broadcastTrade(Mockito.argThat((Trade trade) -> trade.getExchange().equals("gateio")));
        Mockito.verify(tradeServer, timeout(30000).atLeast(1)).broadcastTrade(Mockito.argThat((Trade trade) -> trade.getExchange().equals("btse")));
        Mockito.verify(tradeServer, timeout(30000).atLeast(1)).broadcastTrade(Mockito.argThat((Trade trade) -> trade.getExchange().equals("bitforex")));
        Mockito.verify(tradeServer, timeout(30000).atLeast(1)).broadcastTrade(Mockito.argThat((Trade trade) -> trade.getExchange().equals("hitbtc")));
        Mockito.verify(tradeServer, timeout(30000).atLeast(1)).broadcastTrade(Mockito.argThat((Trade trade) -> trade.getExchange().equals("huobi")));
        Mockito.verify(tradeServer, timeout(30000).atLeast(1)).broadcastTrade(Mockito.argThat((Trade trade) -> trade.getExchange().equals("bitfinex")));
    }


}
