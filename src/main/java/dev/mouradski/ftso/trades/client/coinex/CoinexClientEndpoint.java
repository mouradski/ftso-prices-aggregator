package dev.mouradski.ftso.trades.client.coinex;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.service.PriceService;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ClientEndpoint
@Component
public class CoinexClientEndpoint extends AbstractClientEndpoint {

    protected CoinexClientEndpoint(PriceService priceSender, @Value("${exchanges}") List<String> exchanges, @Value("${assets}") List<String> assets) {
        super(priceSender, exchanges, assets);
    }

    @Override
    protected String getUri() {
        return "wss://socket.coinex.com";
    }

    @Override
    protected void subscribe() {
        var pairs = new ArrayList<String>();

        getAssets(true).forEach(symbol -> {
            getAllQuotesExceptBusd(true).forEach(quote -> {
                pairs.add("\"" + symbol + quote + "\"");
            });
        });

        this.sendMessage("{   \"method\": \"deals.subscribe\",   \"params\": [PAIRS],   \"id\": ID }"
                .replace("PAIRS", pairs.stream().collect(Collectors.joining(",")))
                .replace("ID", counter.getCount().toString()));
    }

    @Override
    protected String getExchange() {
        return "coinex";
    }

    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {
        if (!message.contains("deals.update")) {
            return new ArrayList<>();
        }

        var dealUpdate = this.objectMapper.readValue(message, DealUpdate.class);

        var symbol = SymbolHelper.getSymbol(dealUpdate.getParams().get(0).toString());

        var trades = new ArrayList<Trade>();

        ((List<Map<String, String>>) dealUpdate.getParams().get(1)).stream()
                .sorted(Comparator.comparing(e -> e.get("time")))
                .forEach(deal -> {
                    trades.add(Trade.builder().exchange(getExchange()).symbol(symbol.getLeft()).quote(symbol.getRight())
                            .price(Double.valueOf(deal.get("price").toString()))
                            .amount(Double.valueOf(deal.get("amount").toString())).build());
                });

        return trades;
    }

    @Scheduled(fixedDelay = 30000)
    public void ping() {
        this.sendMessage("{\"method\":\"server.ping\",\"params\":[],\"id\": ID}".replace("ID", counter.getCount().toString()));
    }
}
