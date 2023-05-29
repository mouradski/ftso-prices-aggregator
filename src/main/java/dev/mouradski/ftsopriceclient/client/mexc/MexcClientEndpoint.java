package dev.mouradski.ftsopriceclient.client.mexc;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftsopriceclient.utils.Constants;
import dev.mouradski.ftsopriceclient.client.AbstractClientEndpoint;
import dev.mouradski.ftsopriceclient.model.Trade;
import dev.mouradski.ftsopriceclient.service.PriceService;
import dev.mouradski.ftsopriceclient.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


@ClientEndpoint
@Component
public class MexcClientEndpoint extends AbstractClientEndpoint {

    protected MexcClientEndpoint(PriceService priceSender) {
        super(priceSender);
    }

    @Override
    protected String getUri() {
        return "wss://wbs.mexc.com/raw/ws";
    }

    @Override
    protected void subscribe() {
        Constants.SYMBOLS.stream().map(String::toUpperCase).forEach(symbol -> {
            Arrays.asList("USD", "USDT", "USDC").forEach(quote -> {
                this.sendMessage("{\"op\":\"sub.deal\", \"symbol\":\"SYMBOL_QUOTE\"}".replace("SYMBOL", symbol).replace("QUOTE", quote));
            });
        });

    }

    @Override
    protected String getExchange() {
        return "mexc";
    }

    @Scheduled(fixedDelay = 5000)
    public void pint() {
        this.sendMessage("ping");
    }

    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {
        if (!message.contains("push.deal")) {
            return new ArrayList<>();
        }

        var tradeData = objectMapper.readValue(message, TradeData.class);

        var symbol = SymbolHelper.getQuote(tradeData.getSymbol());

        var trades = new ArrayList<Trade>();


        tradeData.getData().getDeals().stream().sorted(Comparator.comparing(Deal::getT)).forEach(deal -> {
            trades.add(Trade.builder().exchange(getExchange()).symbol(symbol.getLeft()).quote(symbol.getRight()).price(deal.getP()).amount(deal.getQ()).build());
        });

        return trades;
    }
}
