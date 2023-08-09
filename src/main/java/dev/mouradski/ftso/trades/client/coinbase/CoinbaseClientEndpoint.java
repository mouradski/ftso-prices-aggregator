package dev.mouradski.ftso.trades.client.coinbase;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
@ClientEndpoint

public class CoinbaseClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://ws-feed.pro.coinbase.com";
    }

    @Override
    protected void subscribe() {

        var pairs = new ArrayList<String>();

        getAssets(true).forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> pairs.add("\"" + base + "-" + quote + "\"")));
        this.sendMessage("{\"type\":\"subscribe\", \"product_ids\":[PAIRS], \"channels\":[\"matches\"]}".replace("PAIRS", pairs.stream().collect(Collectors.joining(","))));
    }

    @Override
    protected String getExchange() {
        return "coinbase";
    }

    @Override
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {

        if (!message.contains("trade_id")) {
            return Optional.empty();
        }

        var tradeMatch = this.objectMapper.readValue(message, TradeMatch.class);

        var pair = SymbolHelper.getPair(tradeMatch.getProductId());

        var time = Instant.parse(tradeMatch.getTime()).toEpochMilli();

        return Optional.of(Collections.singletonList(Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).price(tradeMatch.getPrice()).amount(tradeMatch.getSize()).timestamp(time).build()));
    }
}
