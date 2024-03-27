package dev.mouradski.ftso.prices.client.bitmart;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.OnMessage;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.Inflater;

@ApplicationScoped
@ClientEndpoint
@Slf4j
@Startup
public class BitmartClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected String getUri() {
        return "wss://ws-manager-compress.bitmart.com/api?protocol=1.1";
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
    protected void subscribeTicker() {
        getAssets(true).forEach(base -> {
            getAllQuotes(true).forEach(quote -> {

                sendMessage("{\"op\": \"subscribe\", \"args\": [\"spot/ticker:BASE_QUOTE\"]}".replace("BASE", base).replace("QUOTE", quote));
            });
        });
    }

    @Override
    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        if (!message.contains("spot/ticker")) {
            return Optional.empty();
        }

        var spotTicker = objectMapper.readValue(message, SpotTicker.class);

        var tickers = new ArrayList<Ticker>();

        spotTicker.getData().forEach(tickerData -> {
            var pair = SymbolHelper.getPair(tickerData.getSymbol());
            tickers.add(Ticker.builder().source(Source.WS).exchange(getExchange()).base(pair.getLeft()).quote(pair.getRight()).lastPrice(Double.parseDouble(tickerData.getLastPrice())).timestamp(currentTimestamp()).build());
        });

        return Optional.of(tickers);
    }

    @Scheduled(every = "15s")
    public void ping() {
        this.sendMessage("ping");
    }
}
