package dev.mouradski.ftso.trades.client.xt;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@ClientEndpoint
public class XtClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://stream.xt.com/public";
    }

    @Override
    protected void subscribe() {
        var pairs = new ArrayList<String>();

        getAssets(false).forEach(base -> getAllQuotesExceptBusd(false).forEach(quote -> {
            pairs.add("\"trade@" + base + "_" + quote + "\"");
        }));

        this.sendMessage("{     \"method\": \"subscribe\",      \"params\": [PAIRS],      \"id\": \"ID\" }"
                .replace("ID", incAndGetIdAsString())
                .replace("PAIRS", pairs.stream().collect(Collectors.joining(","))));
    }

    @Override
    protected String getExchange() {
        return "xt";
    }


    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {

        if (!message.contains("\"topic\":\"trade\"")) {
            return new ArrayList<>();
        }

        var eventData = this.objectMapper.readValue(message, EventData.class);

        var pair = SymbolHelper.getPair(eventData.getEvent().replace("trade@", ""));

        return Arrays.asList(Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).price(eventData.getData().getPrice()).amount(eventData.getData().getQuantity()).timestamp(currentTimestamp()).build());
    }

    @Scheduled(every="20s")
    public void ping() {
        this.sendMessage("ping");
    }
}
