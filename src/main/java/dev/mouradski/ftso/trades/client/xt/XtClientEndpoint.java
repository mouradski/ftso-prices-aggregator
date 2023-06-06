package dev.mouradski.ftso.trades.client.xt;

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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ClientEndpoint
@Component
public class XtClientEndpoint extends AbstractClientEndpoint {

    protected XtClientEndpoint(PriceService priceSender, @Value("${exchanges}") List<String> exchanges, @Value("${assets}") List<String> assets) {
        super(priceSender, exchanges, assets);
    }

    @Override
    protected String getUri() {
        return "wss://stream.xt.com/public";
    }

    @Override
    protected void subscribe() {
        var pairs = new ArrayList<String>();

        getAssets(false).forEach(symbol -> {
            getAllQuotesExceptBusd(false).forEach(quote -> {
                pairs.add("\"trade@" + symbol + "_" + quote + "\"");
            });
        });

        this.sendMessage("{     \"method\": \"subscribe\",      \"params\": [PAIRS],      \"id\": \"ID\" }"
                .replace("ID", counter.getCount().toString())
                .replace("PAIRS", pairs.stream().collect(Collectors.joining(","))));
    }

    @Override
    protected String getExchange() {
        return "xt";
    }


    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {

        if (message.toLowerCase().contains("pong")) {
            System.out.println(message);
        }
        if (!message.contains("\"topic\":\"trade\"")) {
            return new ArrayList<>();
        }

        var eventData = this.objectMapper.readValue(message, EventData.class);

        var symbol = SymbolHelper.getSymbol(eventData.getEvent().replace("trade@", ""));

        return Arrays.asList(Trade.builder().exchange(getExchange()).symbol(symbol.getLeft()).quote(symbol.getRight()).price(eventData.getData().getPrice()).amount(eventData.getData().getQuantity()).build());
    }

    @Scheduled(fixedDelay = 20000)
    public void ping() {
        this.sendMessage("ping");
    }
}
