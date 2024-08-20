package dev.mouradski.ftso.prices.client.ascendex;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@ClientEndpoint
@Startup
public class AscendexClientEndpoint extends AbstractClientEndpoint {
    @Override
    protected String getUri() {
        return "wss://ascendex.com/0/api/pro/v1/stream";
    }

    @Override
    protected String getExchange() {
        return "ascendex";
    }

    @Override
    protected void subscribeTicker() {
        getAssets(true).stream().forEach(base -> getAllStablecoinQuotesExceptBusd(true).forEach(quote -> this.sendMessage(
                """
                  { "op": "sub", "id": "ID", "ch": "summary:BASE/QUOTE" }
                """
                        .replace("ID", incAndGetIdAsString())
                        .replace("BASE", base)
                        .replace("QUOTE", quote))));
    }


    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("summary")) {
            return Optional.empty();
        }

        var summaryMessage = objectMapper.readValue(message, SummaryMessage.class);

        var pair = SymbolHelper.getPair(summaryMessage.getS());

        var ticker = Ticker.builder().source(Source.WS).exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(Double.parseDouble(summaryMessage.getData().getC())).timestamp(currentTimestamp()).build();

        return Optional.of(Collections.singletonList(ticker));
    }

    @Override
    protected boolean pong(String message) {
        if (message.contains("\"ping\"")) {
            sendMessage("{ \"op\": \"pong\" }");
            return true;
        } else {
            return false;
        }
    }
}
