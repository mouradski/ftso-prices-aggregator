package dev.mouradski.ftso.trades.client.xt;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ClientEndpoint
@Component
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
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {

        if (!message.contains("\"topic\":\"trade\"")) {
            return Optional.empty();
        }

        var eventData = this.objectMapper.readValue(message, EventData.class);

        var pair = SymbolHelper.getPair(eventData.getEvent().replace("trade@", ""));

        return Optional.of(Collections.singletonList(Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).price(eventData.getData().getPrice()).amount(eventData.getData().getQuantity()).timestamp(currentTimestamp()).build()));
    }

    @Scheduled(fixedDelay = 20000)
    public void ping() {
        this.sendMessage("ping");
    }
}
