package dev.mouradski.ftso.trades.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.service.TradeService;
import dev.mouradski.ftso.trades.utils.Constants;
import jakarta.websocket.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

import static dev.mouradski.ftso.trades.utils.Constants.SYMBOLS;

@Slf4j
@EnableScheduling
public abstract class AbstractClientEndpoint {

    public static final Gson gson = new Gson();
    private static final long DEFAULT_TIMEOUT = 120; // timeout in seconds
    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected TradeService priceSender;
    protected List<String> assets;
    protected Session userSession = null;
    protected AtomicInteger counter = new AtomicInteger();
    private long lastMessageTime;
    private int retries = 3;

    protected AbstractClientEndpoint() {
    }

    @OnOpen
    public void onOpen(Session userSession) {
        userSession.setMaxTextMessageBufferSize(1024 * 1024 * 10);
        log.info("Opening websocket for {} ....", getExchange());
        this.userSession = userSession;
        var executor = Executors.newSingleThreadScheduledExecutor();
        this.lastMessageTime = System.currentTimeMillis();
        executor.scheduleAtFixedRate(() -> {
            if (this.userSession != null && this.userSession.isOpen() && System.currentTimeMillis() - lastMessageTime > getTimeout() * 1000) {

                log.info("No message received for {} seconds. Reconnecting...", getTimeout());

                onClose(userSession, new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Timeout"));

                connect();
            }
        }, getTimeout(), getTimeout(), TimeUnit.SECONDS);

        log.info("Connected to {}", getExchange());

        subscribe();
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        log.info("Closing websocket for {}, Reason : {}", getExchange(), reason.getReasonPhrase());

        try {
            Thread.sleep(2000);
            this.userSession = null;
        } catch (Exception e) {
        }

        connect();
    }

    @OnMessage
    public void onMessage(String message) throws JsonProcessingException {
        try {
            lastMessageTime = System.currentTimeMillis();

            this.decodeMetadata(message);

            if (!this.pong(message)) {
                this.mapTrade(message).ifPresent(tradeList -> tradeList.forEach(this.priceSender::pushTrade));
            }

        } catch (Exception e) {
            log.debug("Caught exception receiving msg from {}, msg : {}", getExchange(), message, e);
        }

    }

    private boolean isGzipCompressed(byte[] data) {
        return data[0] == (byte) 0x1f && data[1] == (byte) 0x8b;
    }

    private String uncompressGzip(byte[] compressed) throws IOException {
        var byteArrayInputStream = new ByteArrayInputStream(compressed);
        var gzipInputStream = new GZIPInputStream(byteArrayInputStream);
        var inputStreamReader = new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8);
        var bufferedReader = new BufferedReader(inputStreamReader);

        var line = "";
        var stringBuilder = new StringBuilder();

        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }

        return stringBuilder.toString();
    }

    @OnMessage
    public void onMessage(ByteBuffer message) throws JsonProcessingException {

        lastMessageTime = System.currentTimeMillis();

        var data = message.array();
        var result = "";

        if (isGzipCompressed(data)) {
            try {
                result = uncompressGzip(data);
                onMessage(result);
            } catch (IOException e) {
            }
        } else {
            var inflater = new Inflater();
            inflater.setInput(data);

            var outputStream = new ByteArrayOutputStream();
            var buffer = new byte[1024 * 10];

            try {
                var count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);

                result = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);

                onMessage(result);
            } catch (Exception e) {
                result = new String(data, StandardCharsets.UTF_8);
                onMessage(result);
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable t) {
        log.error("Error from {} : {}", getExchange(), t.getMessage());
    }

    protected boolean pong(String message) {
        return false;
    }

    protected void prepareConnection() {
    }

    protected void sendMessage(String message) {
        if (userSession != null && this.userSession.isOpen()) {
            try {
                log.debug("Sending message to {}, payload : {}", getExchange(), message);
                this.userSession.getAsyncRemote().sendText(message);
            } catch (Exception e) {
                log.debug("Caught exception sending msg to {}, msg : {}", getExchange());
            }
        }
    }

    // Implementations must sort extracted trades before returning them
    // Use timestamp or id sent by the exchange to do that
    protected Optional<List<Trade>> mapTrade(String message) throws JsonProcessingException {
        return Optional.empty();
    }

    protected Optional<List<Trade>>  mapTrade(ByteBuffer message) throws JsonProcessingException {
        return Optional.empty();
    }

    protected void decodeMetadata(String message) {
    }


    protected abstract String getUri();

    protected abstract void subscribe();

    protected abstract String getExchange();

    protected void start(TradeService priceSender, List<String> exchanges, List<String> assets) {

        this.priceSender = priceSender;
        this.assets = assets;

        if (exchanges == null || exchanges.contains(getExchange())) {
            try {
                while (retries-- > 0) {
                    if (this.connect()) {
                        break;
                    }
                }
            } catch (Exception e) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    protected boolean connect() {

        prepareConnection();

        try {
            var container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, new URI(getUri()));
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    protected long getTimeout() {
        return DEFAULT_TIMEOUT;
    }

    protected List<String> getAssets() {
        return getAssets(false);
    }

    protected List<String> getAssets(boolean upperCase) {
        List<String> calculatedAssets = assets == null ? SYMBOLS : assets;

        return upperCase ? calculatedAssets.stream().map(String::toUpperCase).toList() : calculatedAssets;
    }

    protected List<String> getAllQuotes(boolean upperCase) {
        return upperCase ? Constants.USD_USDT_USDC_BUSD.stream().map(String::toUpperCase).toList()
                : Constants.USD_USDT_USDC_BUSD;
    }

    protected List<String> getAllQuotesExceptBusd(boolean upperCase) {
        return upperCase ? Constants.USD_USDT_USDC.stream().map(String::toUpperCase).toList()
                : Constants.USD_USDT_USDC;
    }

    protected Integer incAndGetId() {
        return counter.incrementAndGet();
    }

    protected String incAndGetIdAsString() {
        return ((Integer) counter.incrementAndGet()).toString();
    }

    protected Long currentTimestamp() {
        return Instant.now().toEpochMilli();
    }
}
