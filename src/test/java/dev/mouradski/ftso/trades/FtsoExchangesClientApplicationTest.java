package dev.mouradski.ftso.trades;

import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.service.PriceService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.mockito.Mockito.timeout;

@SpringBootTest
public class FtsoExchangesClientApplicationTest {

    @MockBean
    private PriceService priceService;

    @Value("${exchanges}")
    private List<String> exchanges;

    @Test
    public void test() {
        exchanges.forEach(exchange -> {
            Mockito.verify(priceService, timeout(60000).atLeastOnce().description(exchange + " will push a trade event")).pushPrice(Mockito.argThat((Trade trade) -> trade.getExchange().equals(exchange)));
        });
    }
}
