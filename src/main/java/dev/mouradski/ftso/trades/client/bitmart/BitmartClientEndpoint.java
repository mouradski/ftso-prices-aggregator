package dev.mouradski.ftso.trades.client.bitmart;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.client.HttpTickers;
import dev.mouradski.ftso.trades.model.Ticker;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.OnMessage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.zip.Inflater;

@ApplicationScoped
@ClientEndpoint
@Slf4j
@Startup
public class BitmartClientEndpoint extends AbstractClientEndpoint implements HttpTickers {


    private List<String> supportedSymbols = new ArrayList<>();

    @Override
    protected String getUri() {
        return "wss://ws-manager-compress.bitmart.com/api?protocol=1.1";
    }

    @Override
    protected void subscribeTrade() {
        var pairs = new ArrayList<String>();

        getAssets(true).stream().map(String::toUpperCase)
                .forEach(base -> getAllQuotesExceptBusd(true).forEach(quote -> {
                    if (supportedSymbols.contains(base + "_" + quote)) {
                        pairs.add("\"spot/trade:" + base + "_" + quote + "\"");
                    }
                }));
        if (pairs.size() < 20) {
            this.sendMessage("{\"op\":\"subscribe\",\"args\":[PAIRS]}".replace("PAIRS",
                    String.join(",", pairs)));

        } else {// break subscription into smaller chunks
            var waitASecondThersASubscriptionLimitOf100SymbolsPer10Seconds = pairs.size() > 100;
            var subscribedPairsAmount = 0;
            for (List<String> pairChunkStrings : chunks(pairs, 10)) {
                this.sendMessage("{\"op\":\"subscribe\",\"args\":[PAIRS]}".replace("PAIRS",
                        String.join(",", pairChunkStrings)));
                subscribedPairsAmount += pairChunkStrings.size();
                if (waitASecondThersASubscriptionLimitOf100SymbolsPer10Seconds && subscribedPairsAmount >= 100) {
                    try {
                        Thread.sleep(10 * 1000);
                        subscribedPairsAmount = 0;
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    @Override
    public void updateTickers() {
        this.lastTickerTime = System.currentTimeMillis();
        this.lastTickerTime = System.currentTimeMillis();

        if (subscribeTicker && exchanges.contains(getExchange())) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api-cloud.bitmart.com/spot/quotation/v3/tickers"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            try {

                var response = client.send(request, HttpResponse.BodyHandlers.ofString());

                var tickers = gson.fromJson(response.body(), TickerResponse.class);

                tickers.getData().forEach(ticker -> {
                    var pair = SymbolHelper.getPair(ticker[0]);

                    if (getAssets(true).contains(pair.getLeft())
                            && getAllQuotesExceptBusd(true).contains(pair.getRight())) {
                        pushTicker(Ticker.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight())
                                .lastPrice(Double.valueOf(ticker[1])).timestamp(currentTimestamp()).build());
                    }
                });

            } catch (IOException | InterruptedException e) {
            }
        }
    }

    @Override
    protected String getExchange() {
        return "bitmart";
    }

    @Override
    @OnMessage
    public void onMessage(ByteBuffer message) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(message);
        byte[] data = new byte[message.remaining()];
        message.get(data);

        try (ByteBufInputStream bis = new ByteBufInputStream(byteBuf)) {
            byte[] temp = new byte[data.length];
            bis.read(temp);
            Inflater decompresser = new Inflater(true);
            decompresser.setInput(temp, 0, temp.length);
            StringBuilder sb = new StringBuilder();
            byte[] result = new byte[1024];

            while (!decompresser.finished()) {
                int resultLength = decompresser.inflate(result);
                sb.append(new String(result, 0, resultLength, StandardCharsets.UTF_8));
            }
            decompresser.end();
            onMessage(sb.toString());
        } catch (Exception e) {
            log.error("Caught exception receiving msg from {}, msg : {}", getExchange(), message, e);
        }
    }

    @Override
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {

        if (!message.contains("data")) {
            return Optional.empty();
        }
        var root = this.objectMapper.readValue(message, Root.class);

        var trades = new ArrayList<Trade>();

        root.getData().stream()
                .sorted(Comparator.comparing(TradeData::getTime))
                .forEach(tradeData -> {
                    var pair = SymbolHelper.getPair(tradeData.getSymbol());
                    trades.add(Trade.builder().exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight())
                            .price(tradeData.getPrice()).amount(tradeData.getSize())
                            .timestamp(currentTimestamp()) // timestamp is in seconds
                            .build());

                });

        return Optional.of(trades);
    }

    @Scheduled(every = "15s")
    public void ping() {
        this.sendMessage("ping");
    }

    @Override
    protected void prepareConnection() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api-cloud.bitmart.com/spot/v1/symbols"))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            dev.mouradski.ftso.trades.client.bitmart.SymbolResponse symbolResponse = objectMapper
                    .readValue(response.body(), SymbolResponse.class);

            this.supportedSymbols = symbolResponse.getData().getSymbols();

        } catch (Exception ignored) {
        }
    }

    private static <T> List<List<T>> chunks(List<T> list, final int L) {
        List<List<T>> parts = new ArrayList<List<T>>();
        final int N = list.size();
        for (int i = 0; i < N; i += L) {
            parts.add(new ArrayList<T>(
                    list.subList(i, Math.min(N, i + L))));
        }
        return parts;
    }
}
