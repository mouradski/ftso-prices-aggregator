package dev.mouradski.ftso.trades.client.whitebit;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.service.TradeService;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ClientEndpoint
@Component
@Slf4j
public class WhitebitClientEndpoint extends AbstractClientEndpoint {

    private Set<String> supportedSymbols;

    protected WhitebitClientEndpoint(TradeService priceSender, @Value("${exchanges}") List<String> exchanges, @Value("${assets}") List<String> assets) {
        super(priceSender, exchanges, assets);
    }

    @Override
    protected String getUri() {
        return "wss://api.whitebit.com/ws";
    }

    @Override
    protected void subscribe() {
        var pairs = new ArrayList<String>();

        getAssets(true).forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> {
            var pair = base + "_" + quote;

            if (supportedSymbols.contains(pair)) {
                pairs.add("\"" + pair + "\"");
            }
        }));

        this.sendMessage("{\"id\": ID,\"method\": \"trades_subscribe\",\"params\": [PAIRS]}"
                .replace("ID", incAndGetIdAsString())
                .replace("PAIRS", pairs.stream().collect(Collectors.joining(","))));

    }

    @Override
    protected String getExchange() {
        return "whitebit";
    }


    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {
        if (!message.contains("trades_update")) {
            return new ArrayList<>();
        }

        var trades = new ArrayList<Trade>();


        var tradeUpdateMessage = this.objectMapper.readValue(message, TradeUpdateMessage.class);

        var pair = SymbolHelper.getPair(tradeUpdateMessage.getParams().get(0).toString());

        ((List<Map>) tradeUpdateMessage.getParams().get(1)).stream()
                .sorted(Comparator.comparing(v -> v.get("time").toString())).forEach(tradeUpdate -> trades.add(Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight())
                        .price(Double.valueOf(tradeUpdate.get("price").toString())).amount(Double.valueOf(tradeUpdate.get("amount").toString())).build()));

        return trades;

    }

    @Override
    protected void prepareConnection() {
        var client = HttpClient.newHttpClient();

        var request = HttpRequest.newBuilder()
                .uri(URI.create("https://whitebit.com/api/v4/public/markets"))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            var markets = gson.fromJson(response.body(), MarketPair[].class);

            this.supportedSymbols = Stream.of(markets).map(MarketPair::getName).collect(Collectors.toSet());

        } catch (IOException | InterruptedException e) {
            log.error("Caught exception collecting markets from {}", getExchange());
        }
    }

    @Scheduled(fixedDelay = 30000)
    public void ping() {
        this.sendMessage("{\"id\": ID,\"method\": \"ping\",\"params\": []}".replace("ID", incAndGetIdAsString()));
    }
}
