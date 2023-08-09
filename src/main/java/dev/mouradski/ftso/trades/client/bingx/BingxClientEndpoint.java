package dev.mouradski.ftso.trades.client.bingx;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@ClientEndpoint
public class BingxClientEndpoint extends AbstractClientEndpoint {
    @Override
    protected String getUri() {
        return "wss://open-api-ws.bingx.com/market";
    }

    @Override
    protected void subscribe() {
        getAssets(true).forEach(base -> {
            getAllQuotesExceptBusd(true).forEach(quote -> {
                this.sendMessage("{ \"id\": \"ID\", \"reqType\": \"sub\", \"dataType\": \"BASE-QUOTE@trade\" }".replace("ID", incAndGetIdAsString()).replace("BASE", base).replace("QUOTE", quote));
            });
        });
    }


    @Override
    protected String getExchange() {
        return "bingx";
    }

    @Override
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {
        if (!message.contains("@trade")) {
            return Optional.empty();
        }

        var tradeResponse = this.objectMapper.readValue(message, TradeResponse.class);

        var pair = SymbolHelper.getPair(tradeResponse.getData().getSymbol());

        return Optional.of(Collections.singletonList(Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight())
                .price(Double.valueOf(tradeResponse.getData().getPrice())).amount(Double.valueOf(tradeResponse.getData().getAmount())).timestamp(currentTimestamp()).build()));
    }

    @Override
    protected boolean pong(String message) {
        if (message.contains("Ping")) {
            this.sendMessage("Pong");
            return true;
        }

        return false;
    }
}
