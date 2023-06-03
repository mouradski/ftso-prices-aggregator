package dev.mouradski.ftso.trades.client.bitmart;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.trades.client.AbstractClientEndpoint;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.service.PriceService;
import dev.mouradski.ftso.trades.utils.SymbolHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.OnMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.Inflater;

@Component
@ClientEndpoint
@Slf4j
public class BitmartClientEndpoint extends AbstractClientEndpoint {

    private List<String> supportedSymbols = new ArrayList<>();

    protected BitmartClientEndpoint(PriceService priceSender, @Value("${exchanges}") List<String> exchanges, @Value("${assets}") List<String> assets) {
        super(priceSender, exchanges, assets);
    }

    @Override
    protected String getUri() {
        return "wss://ws-manager-compress.bitmart.com/api?protocol=1.1";
    }

    @Override
    protected void subscribe() {
        var pairs = new ArrayList<String>();

        getAssets(true).forEach(symbol -> {
            Arrays.asList("USDT").forEach(quote -> {
                if (supportedSymbols.contains(symbol + "_" + quote)) {
                    pairs.add("\"spot/trade:" + symbol + "_" + quote + "\"");
                }
            });
        });


        this.sendMessage("{\"op\":\"subscribe\",\"args\":[PAIRS]}".replace("PAIRS", pairs.stream().collect(Collectors.joining(","))));

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
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {

        if (!message.contains("data")) {
            return new ArrayList<>();
        }
        var root = this.objectMapper.readValue(message, Root.class);

        var trades = new ArrayList<Trade>();

        root.getData().stream()
                .sorted(Comparator.comparing(TradeData::getTime))
                .forEach(tradeData -> {
                    var symbol = SymbolHelper.getSymbol(tradeData.getSymbol());
                    trades.add(Trade.builder().exchange(getExchange()).symbol(symbol.getLeft()).quote(symbol.getRight())
                            .price(tradeData.getPrice()).amount(tradeData.getSize()).build());

                });

        return trades;
    }

    @Scheduled(fixedDelay = 15000)
    public void ping() {
        this.sendMessage("ping");
    }


    @Override
    protected void prepareConnection() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api-cloud.bitmart.com/spot/v1/symbols"))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            dev.mouradski.ftso.trades.client.bitmart.SymbolResponse symbolResponse = objectMapper.readValue(response.body(), SymbolResponse.class);

            this.supportedSymbols = symbolResponse.getData().getSymbols();

        } catch (Exception e) {
        }
    }
}
