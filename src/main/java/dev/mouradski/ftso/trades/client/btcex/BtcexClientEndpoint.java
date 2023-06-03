package dev.mouradski.ftso.trades.client.btcex;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.service.PriceService;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@ClientEndpoint
public class BtcexClientEndpoint extends AbstractClientEndpoint {

    protected BtcexClientEndpoint(PriceService priceSender, @Value("${exchanges}") List<String> exchanges, @Value("${assets}") List<String> assets) {
        super(priceSender, exchanges, assets);
    }

    @Override
    protected String getUri() {
        return "wss://api.btcex.com/ws/api/v1";
    }

    @Override
    protected void subscribe() {
        getAssets().forEach(symbol -> {
            getAllQuotesExceptBusd(false).forEach(quote -> {
                var msg = "{\"jsonrpc\" : \"2.0\",\"id\" : ID,\"method\" : \"/public/subscribe\",\"params\" : {\"channels\":[\"price_index.SYMBOL_QUOTE\"]}}"
                        .replace("ID", counter.getCount().toString())
                        .replace("SYMBOL", symbol).replace("QUOTE", quote);
                this.sendMessage(msg);
            });
        });
    }

    @Override
    protected String getExchange() {
        return "btcex";
    }


    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {
        if (!message.contains("index_name")) {
            return new ArrayList<>();
        }

        dev.mouradski.ftso.trades.client.btcex.Root root = objectMapper.readValue(message, Root.class);

        Pair<String, String> symbol = SymbolHelper.getSymbol(root.getParams().getData().getIndex_name());

        return Arrays.asList(Trade.builder().exchange(getExchange()).symbol(symbol.getLeft()).quote(symbol.getRight()).price(root.getParams().getData().getPrice()).amount(0d).build());
    }

    @Scheduled(fixedDelay = 15000)
    public void ping() {
        this.sendMessage("{ \"jsonrpc\":\"2.0\",\"id\": ID,\"method\": \"/public/ping\",\"params\":{}}".replace("ID", counter.getCount().toString()));
    }
}
