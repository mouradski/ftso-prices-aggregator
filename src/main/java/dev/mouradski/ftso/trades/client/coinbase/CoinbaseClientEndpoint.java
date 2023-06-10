package dev.mouradski.ftso.trades.client.coinbase;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.service.TradeService;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ClientEndpoint
@Component
public class CoinbaseClientEndpoint extends AbstractClientEndpoint {

    protected CoinbaseClientEndpoint(TradeService priceSender, @Value("${exchanges}") List<String> exchanges, @Value("${assets}") List<String> assets) {
        super(priceSender, exchanges, assets);
    }

    @Override
    protected String getUri() {
        return "wss://ws-feed.pro.coinbase.com";
    }

    @Override
    protected void subscribe() {

        var pairs = new ArrayList<String>();

        getAssets(true).forEach(base -> {
            getAllQuotesExceptBusd(true).forEach(quote -> {
                pairs.add("\"" + base + "-" + quote + "\"");
            });
        });
        this.sendMessage("{\"type\":\"subscribe\", \"product_ids\":[PAIRS], \"channels\":[\"matches\"]}".replace("PAIRS", pairs.stream().collect(Collectors.joining(","))));
    }

    @Override
    protected String getExchange() {
        return "coinbase";
    }

    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {

        if (!message.contains("trade_id")) {
            return new ArrayList<>();
        }

        var tradeMatch = this.objectMapper.readValue(message, TradeMatch.class);

        var pair = SymbolHelper.getPair(tradeMatch.getProductId());

        return Arrays.asList(Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).price(tradeMatch.getPrice()).amount(tradeMatch.getSize()).build());
    }
}
