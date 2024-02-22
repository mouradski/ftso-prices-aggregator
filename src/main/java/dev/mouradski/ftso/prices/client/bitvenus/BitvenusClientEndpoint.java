package dev.mouradski.ftso.prices.client.bitvenus;

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
public class BitvenusClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://wsapi.bitvenus.me/openapi/quote/ws/v1";
    }

    @Override
    protected String getExchange() {
        return "bitvenus";
    }

    @Override
    protected void subscribeTicker() {
        getAssets(true).stream().forEach(base -> getAllStablecoinQuotesExceptBusd(true).forEach(quote -> this.sendMessage(
                "{   \"symbol\" : \"SYMBOL\",   \"topic\" : \"realtimes\",   \"event\" : \"sub\",   \"params\" : {       \"binary\" : \"false\"   } }"
                        .replace("SYMBOL", base + quote))));
    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {

        if (!message.contains("realtimes")) {
            return Optional.empty();
        }

        var tickerResponse = objectMapper.readValue(message, TickerResponse.class);
        var pair = SymbolHelper.getPair(tickerResponse.getSymbol());

        var tickers = new ArrayList<Ticker>();

        for (var data : tickerResponse.getData()) {
            tickers.add(Ticker.builder().base(pair.getLeft()).quote(pair.getRight()).exchange(getExchange()).lastPrice(Double.parseDouble(data.getC())).timestamp(currentTimestamp()).build());
        }

        return Optional.of(tickers);
    }

    @Scheduled(every = "290s")
    public void ping() {
        this.sendMessage("\"ping\": ID }".replace("ID", incAndGetIdAsString()));
    }

    @Override
    protected boolean pong(String message) {
        return message.contains("pong");
    }
}
