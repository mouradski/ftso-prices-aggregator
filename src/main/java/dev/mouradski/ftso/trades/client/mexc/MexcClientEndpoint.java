package dev.mouradski.ftso.trades.client.mexc;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import jakarta.websocket.ClientEndpoint;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@ClientEndpoint
@Component
public class MexcClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://wbs.mexc.com/raw/ws";
    }

    @Override
    protected void subscribe() {
        getAssets(true).forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> this.sendMessage("{\"op\":\"sub.deal\", \"symbol\":\"SYMBOL_QUOTE\"}".replace("SYMBOL", base).replace("QUOTE", quote))));
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
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {
        if (!message.contains("push.deal")) {
            return Optional.empty();
        }

        var tradeData = objectMapper.readValue(message, TradeData.class);

        var pair = SymbolHelper.getPair(tradeData.getSymbol());

        var trades = new ArrayList<Trade>();

        tradeData.getData().getDeals().stream().sorted(Comparator.comparing(Deal::getT)).forEach(deal -> {
            trades.add(Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).price(deal.getP()).amount(deal.getQ()).timestamp(currentTimestamp()).build());
        });

        return Optional.of(trades);
    }
}
