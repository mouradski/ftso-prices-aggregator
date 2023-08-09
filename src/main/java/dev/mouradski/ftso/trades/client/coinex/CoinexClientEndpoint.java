package dev.mouradski.ftso.trades.client.coinex;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@ClientEndpoint
@Component
public class CoinexClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://socket.coinex.com";
    }

    @Override
    protected void subscribe() {
        var pairs = new ArrayList<String>();

        getAssets(true).forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> pairs.add("\"" + base + quote + "\"")));

        this.sendMessage("{   \"method\": \"deals.subscribe\",   \"params\": [PAIRS],   \"id\": ID }"
                .replace("PAIRS", pairs.stream().collect(Collectors.joining(",")))
                .replace("ID", incAndGetIdAsString()));
    }

    @Override
    protected String getExchange() {
        return "coinex";
    }

    @Override
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {
        if (!message.contains("deals.update")) {
            return Optional.empty();
        }

        var dealUpdate = this.objectMapper.readValue(message, DealUpdate.class);

        var pair = SymbolHelper.getPair(dealUpdate.getParams().get(0).toString());

        var trades = new ArrayList<Trade>();

        ((List<Map<String, String>>) dealUpdate.getParams().get(1)).stream()
                .sorted(Comparator.comparing(e -> e.get("time")))
                .forEach(deal -> trades.add(Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight())
                        .price(Double.valueOf(deal.get("price").toString()))
                        .amount(Double.valueOf(deal.get("amount").toString()))
                        .timestamp(currentTimestamp()).build()));

        return Optional.of(trades);
    }

    @Scheduled(fixedDelay = 30000)
    public void ping() {
        this.sendMessage("{\"method\":\"server.ping\",\"params\":[],\"id\": ID}".replace("ID", incAndGetIdAsString()));
    }
}
