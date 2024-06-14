package dev.mouradski.ftso.prices.client.bybit;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.util.Date;

@ApplicationScoped
@ClientEndpoint
@Startup
public class BybitClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://stream.bybit.com/v5/public/spot";
    }


    @Override
    protected void subscribeTicker() {
        var subscribeMsgTemplate = """
                {
                    "op": "subscribe","args": [
                        "tickers.SYMBOL"
                    ]
                }
                """;

        this.getAssets(true).forEach(base -> {
            this.getAllQuotes(true).forEach(quote -> {
                this.sendMessage(subscribeMsgTemplate.replace("SYMBOL", base + quote));
            });
        });
        super.subscribeTicker();
    }


    @Override
    public void onMessage(String message) throws JsonProcessingException {
        if (!message.contains("tickers.")) {
            return;
        }

        var tickerPayload = objectMapper.readValue(message, dev.mouradski.ftso.prices.client.bybit.Ticker.class);

        var pair = SymbolHelper.getPair(tickerPayload.getData().getSymbol());


        pushTicker(Ticker.builder().base(pair.getLeft()).quote(pair.getRight()).source(Source.WS).exchange(getExchange()).lastPrice(Double.parseDouble(tickerPayload.getData().getLastPrice())).build());
    }

    @Override
    protected String getExchange() {
        return "bybit";
    }

    @Scheduled(every="30s")
    public void ping() {
        this.sendMessage("{\"ping\":" + new Date().getTime() + "}");
    }
}
