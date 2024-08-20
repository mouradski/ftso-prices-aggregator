package dev.mouradski.ftso.prices.client.coinstore;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

@ApplicationScoped
@ClientEndpoint
@Startup
public class CoinstoreClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://ws.coinstore.com/s/ws";
    }

    @Override
    protected String getExchange() {
        return "coinstore";
    }

    @Override
    protected void subscribeTicker() {
        var subscribeMsgTemplate = """
                {
                  "op": "SUB",
                  "channel": [
                    "BASEQUOTE@ticker"
                  ],
                  "id": _ID_
                }
                """;

        getAssets(false).forEach(base -> {
            getAllQuotes(false).forEach(quote -> {
                this.sendMessage(subscribeMsgTemplate.replace("BASE", base).replace("QUOTE", quote).replace("_ID_", incAndGetIdAsString()));
            });
        });
    }

    @Override
    public void onMessage(String message) throws JsonProcessingException {

        this.messageReceived();

        if (!message.contains("@ticker")) {
            return;
        }

        var tickerData = objectMapper.readValue(message, TickerData.class);

        if (tickerData.getSymbol() == null || tickerData.getClose() == null) {
            return;
        }
        
        var pair = SymbolHelper.getPair(tickerData.getSymbol());

        pushTicker(Ticker.builder().exchange(getExchange()).timestamp(currentTimestamp()).base(pair.getLeft()).quote(pair.getRight()).source(Source.WS).lastPrice(Double.parseDouble(tickerData.getClose())).build());
    }


    @Override
    protected boolean pong(String message) {
        if (message.contains("ping")) {
            sendMessage("""
                    {
                      "op": "pong",
                      "epochMillis": TIMESTAMP
                    }
                    """.replace("TIMESTAMP", currentTimestamp().toString()));
            return true;
        }

        return false;
    }
}
