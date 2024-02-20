package dev.mouradski.ftso.prices.client.bitget;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@ClientEndpoint
@Startup
public class BitgetClientEndpoint extends AbstractClientEndpoint {

    private boolean subscribed;

    @Override
    protected String getUri() {
        return "wss://ws.bitget.com/spot/v1/stream";
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


        getAssets(true).forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> {
            args.add("{\"instType\": \"sp\",\"channel\": \"ticker\",\"instId\": \"PAIR\"}".replace("PAIR", base + quote));
        }));



        this.sendMessage("{\"op\": \"subscribe\",\"args\": [ARGS]}".replace("ARGS", String.join(",", args)));
    }



    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("open24h")) {
            return Optional.empty();
        }

        var tickerResponse = objectMapper.readValue(message, TickerResponse.class);

        var tickers = new ArrayList<Ticker>();

        var pair = SymbolHelper.getPair(tickerResponse.getArg().getInstId());

        for (var ticker : tickerResponse.getData()) {
            tickers.add(Ticker.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(ticker.getLast()).timestamp(currentTimestamp()).build());
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
