package dev.mouradski.ftso.trades;

import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.service.TradeService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.mockito.Mockito.timeout;

@SpringBootTest
@Slf4j
class FtsoExchangesClientApplicationTest {

    @MockBean
    private TradeService priceService;

    @Value("${exchanges}")
    private List<String> exchanges;

    @Test
    void test() {
        exchanges.forEach(exchange -> {
            Mockito.verify(priceService, timeout(60000).atLeastOnce().description(exchange + " will push a trade event")).pushTrade(Mockito.argThat((Trade trade) -> trade.getExchange().equals(exchange)));
            log.info("At least one message was received from {} exchange", exchange);
        });
    }
}
