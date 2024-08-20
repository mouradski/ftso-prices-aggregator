package dev.mouradski.ftso.prices.client.bitget;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Singleton;
import jakarta.websocket.ClientEndpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
@ClientEndpoint
@Startup
public class BitgetClientEndpoint extends AbstractClientEndpoint {

    private boolean subscribed;

    @Override
    protected String getUri() {
        return "wss://ws.bitget.com/v2/ws/public";
    }

    @Override
    protected void subscribeTicker() {
        subscribeTradeTickers();
    }

    private void subscribeTradeTickers() {
        if (subscribed) {
            return;
        }
        subscribed = true;

        var args = new ArrayList<String>();

        var templateSubArg = """
                {
                  "instType": "INST_TYPE",
                  "channel": "ticker",
                  "instId": "PAIR"
                }
                """;

        getAssets(true).forEach(base -> getAllQuotes(true).forEach(quote -> {
            args.add(templateSubArg.replace("INST_TYPE", "SPOT").replace("PAIR", base + quote));
            args.add(templateSubArg.replace("INST_TYPE", quote + "-FUTURES").replace("PAIR", base + quote));
        }));


        this.sendMessage("{\"op\": \"subscribe\",\"args\": [ARGS]}".replace("ARGS", String.join(",", args)));
    }


    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {

        if (!message.contains("open24h")) {
            return Optional.empty();
        }

        var future = message.contains("-FUTURES");

        var tickerMessage = objectMapper.readValue(message, TickerMessage.class);

        var tickers = new ArrayList<Ticker>();



        for (var ticker : tickerMessage.getData()) {
            var pair = SymbolHelper.getPair(ticker.getInstId());
            tickers.add(Ticker.builder().source(Source.WS).exchange(getExchange() + (future ? "future" : "")).base(pair.getLeft()).quote(pair.getRight()).lastPrice(Double.valueOf(ticker.getLastPr())).timestamp(currentTimestamp()).build());
        }

        return Optional.of(tickers);
    }

    @Override
    protected String getExchange() {
        return "bitget";
    }

    @Scheduled(every = "30s")
    public void ping() {
        this.sendMessage("ping");
    }
}
