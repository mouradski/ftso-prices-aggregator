package dev.mouradski.ftso.trades.client.p2b;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@ClientEndpoint
public class P2BClientEndpoint extends AbstractClientEndpoint {
    @Override
    protected String getUri() {
        return "wss://apiws.p2pb2b.com";
    }

    @Override
    protected void subscribe() {
        var subscribeTemplate = "{\"method\":\"deals.subscribe\",\"params\":[SYMBOLS],\"id\":ID}";
        
        var symbols = new ArrayList<String>();

        getAssets(true).forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> symbols.add("\"" + base + "_" + quote + "\"")));

        this.sendMessage(subscribeTemplate.replace("SYMBOLS", String.join(",", symbols)).replace("ID", incAndGetIdAsString()));
    }

    @Override
    protected String getExchange() {
        return "p2b";
    }

    @Override
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {
        if (!message.contains("deals.update")) {
            return Optional.empty();

        }

        var trades = new ArrayList<Trade>();

        var dealsResponse = objectMapper.readValue(message, DealsResponse.class);

        var pair = SymbolHelper.getPair(dealsResponse.getParams().get(0).toString());

        var deals = objectMapper.convertValue(dealsResponse.getParams().get(1), new TypeReference<List<Deal>>(){});

        deals.stream().sorted(Comparator.comparing(Deal::getTime)).forEach(deal -> trades.add(Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight())
                .price(Double.valueOf(deal.getPrice())).amount(Double.valueOf(deal.getAmount())).timestamp(currentTimestamp()).build()));

        return Optional.of(trades);
    }

    @Scheduled(every = "30s")
    public void ping() {
        this.sendMessage("{\"method\":\"server.ping\",\"params\":[],\"id\":ID}".replace("ID", incAndGetIdAsString()));
    }
}
