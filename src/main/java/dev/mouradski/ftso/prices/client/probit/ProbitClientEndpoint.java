package dev.mouradski.ftso.prices.client.probit;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@ClientEndpoint
@Startup
public class ProbitClientEndpoint extends AbstractClientEndpoint {
    @Override
    protected String getUri() {
        return "wss://api.probit.com/api/exchange/v1/ws";
    }

    @Override
    protected String getExchange() {
        return "probit";
    }

    @Override
    protected void subscribeTicker() {
        getAssets(true).forEach(base -> {
            getAllQuotes(true).forEach(quote -> {
               this.sendMessage("""
                       {
                         "type": "subscribe",
                         "channel": "marketdata",
                         "interval": 500,
                         "market_id": "MARKET_ID",
                         "filter": [
                           "ticker"
                         ]
                       }
                       """.replace("MARKET_ID", base + "-" + quote));
            });
        });
    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        var marketData = objectMapper.readValue(message, MarketData.class);

        if ("marketdata".equals(marketData.getChannel())) {
            var par = SymbolHelper.getPair(marketData.getMarketId());
            return Optional.of(List.of(Ticker.builder()
                    .exchange(getExchange())
                    .source(Source.WS)
                    .base(par.getLeft())
                    .quote(par.getRight())
                    .lastPrice(Double.parseDouble(marketData.getTicker().getLast()))
                    .timestamp(currentTimestamp())
                    .build()));
        }

        return Optional.empty();
    }
}
