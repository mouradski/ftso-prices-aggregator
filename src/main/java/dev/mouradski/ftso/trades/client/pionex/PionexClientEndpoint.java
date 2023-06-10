package dev.mouradski.ftso.trades.client.pionex;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.service.TradeService;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@ClientEndpoint
@Component
@Slf4j
public class PionexClientEndpoint extends AbstractClientEndpoint {

    protected Set<String> supportedSymbols;

    protected PionexClientEndpoint(TradeService priceSender, @Value("${exchanges}") List<String> exchanges, @Value("${assets}") List<String> assets) {
        super(priceSender, exchanges, assets);
    }

    @Override
    protected String getUri() {
        return "wss://ws.pionex.com/wsPub";
    }

    @Override
    protected void subscribe() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        getAssets(true).stream()
                .filter(base -> !getAllQuotes(true).contains(base))
                .forEach(base -> {
                    getAllQuotesExceptBusd(true).forEach(quote -> {
                        executorService.submit(() -> {
                            try {
                                var symbolId = base + "_" + quote;
                                if (supportedSymbols.contains(symbolId)) {
                                    Thread.sleep(counter.getCount() * 500);
                                    this.sendMessage("{\"op\": \"SUBSCRIBE\",\"topic\":  \"TRADE\", \"symbol\": \"SYMBOL\"}".replace("SYMBOL", symbolId));
                                }

                            } catch (InterruptedException e) {
                            }
                        });
                    });
                });
    }

    @Override
    protected String getExchange() {
        return "pionex";
    }

    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {
        if (!message.contains("tradeId")) {
            return new ArrayList<>();
        }

        var tradeResponse = objectMapper.readValue(message, TradeResponse.class);

        var trades = new ArrayList<Trade>();

        tradeResponse.getData().forEach(tradeData -> {
            var pair = SymbolHelper.getPair(tradeData.getSymbol());
            trades.add(Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).price(tradeData.getPrice()).amount(tradeData.getSize()).build());
        });

        return trades;
    }

    @Override
    protected boolean pong(String message) {
        if (message.contains("PING")) {
            this.sendMessage("{\"op\": \"PONG\", \"timestamp\": TIME}".replace("TIME", new Date().getTime() + ""));
            return true;
        }

        return false;
    }

    @Override
    protected void prepareConnection() {
        var client = HttpClient.newHttpClient();

        var request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.pionex.com/api/v1/common/symbols"))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            var symbolsResponse = gson.fromJson(response.body(), SymbolsResponse.class);

            this.supportedSymbols = symbolsResponse.getData().getSymbols().stream().map(SymbolData::getSymbol).collect(Collectors.toSet());

        } catch (IOException | InterruptedException e) {
            log.error("Caught exception receiving symbols list from {}", getExchange(), e);
        }

    }
}
