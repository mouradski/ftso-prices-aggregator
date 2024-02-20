package dev.mouradski.ftso.trades.client.orangex;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Ticker;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@ClientEndpoint
@Startup
public class OrangexClientEndpoint extends AbstractClientEndpoint  {
    @Override
    protected String getUri() {
        return "wss://api.orangex.com/ws/api/v1";
    }

    @Override
    protected String getExchange() {
        return "orangex";
    }

    @Override
    protected void subscribeTicker() {
        getAssets(true).forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> this.sendMessage("{\"jsonrpc\" : \"2.0\",   \"id\" : ID,   \"method\" : \"/public/subscribe\",   \"params\" : {     \"channels\":[       \"ticker.BASE-QUOTE.raw\"]   } }".replace("BASE", base).replace("QUOTE", quote).replace("ID", incAndGetIdAsString()))));
    }

    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {

        if (message.toLowerCase().contains("ping")) {
            System.out.println(message);
        }

        if (!message.contains("last_price")) {
            return Optional.empty();
        }

        var tickerData = objectMapper.readValue(message, dev.mouradski.ftso.trades.client.orangex.Ticker.class);

        var pair = SymbolHelper.getPair(tickerData.getParams().getData().getInstrumentName());

        var ticker = Ticker.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(Double.parseDouble(tickerData.getParams().getData().getLastPrice())).timestamp(currentTimestamp()).build();

        return Optional.of(Arrays.asList(ticker));
    }

    @Scheduled(every = "15s")
    public void ping() {
        var pingMsg = "{ \"jsonrpc\":\"2.0\",\"id\": ID, \"method\": \"/public/ping\", \"params\":{} }".replace("ID", incAndGetIdAsString());
        sendMessage(pingMsg);
    }
}
