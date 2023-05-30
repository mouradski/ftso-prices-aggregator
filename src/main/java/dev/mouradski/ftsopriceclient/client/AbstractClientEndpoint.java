package dev.mouradski.ftsopriceclient.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.mouradski.ftsopriceclient.utils.Constants;
import dev.mouradski.ftsopriceclient.model.Trade;
import dev.mouradski.ftsopriceclient.service.PriceService;
import dev.mouradski.ftsopriceclient.utils.Counter;
import jakarta.websocket.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

@Slf4j
@EnableScheduling
public abstract class AbstractClientEndpoint {

    private static final long DEFAULT_TIMEOUT = 120; // timeout in seconds
    protected Session userSession = null;
    private ScheduledExecutorService executor;
    private long lastMessageTime;
    protected Counter counter = new Counter();

    protected final PriceService priceSender;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected AbstractClientEndpoint(PriceService priceSender) {
        this.priceSender = priceSender;
        this.connect();
    }

    @OnOpen
    public void onOpen(Session userSession) {
        userSession.setMaxTextMessageBufferSize(1024 * 1024);
        log.info("Opening websocket for {} ....", getExchange());
        this.userSession = userSession;
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.lastMessageTime = System.currentTimeMillis();
        executor.scheduleAtFixedRate(() -> {
            if (System.currentTimeMillis() - lastMessageTime > getTimeout() * 1000) {

                log.info("No message received for {} seconds. Reconnecting...", getTimeout());

                onClose(userSession, new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Timeout"));

                connect();
            }
        }, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, TimeUnit.SECONDS);

        log.info("Connected to {}", getExchange());

        subscribe();
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        log.info("Closing websocket for {}, Reason : {}",  getExchange(), reason.getReasonPhrase());

        try {
            // when the method is called programmatically
            // we make sure that the session is closed before creating a new one
            if (this.userSession != null) {
                this.userSession.close();
            }
            Thread.sleep(1000);
        } catch (Exception e) {
        }

        this.userSession = null;


        connect();
    }

    @OnMessage
    public void onMessage(String message) throws JsonProcessingException {
        lastMessageTime = System.currentTimeMillis();

        this.decodeMetadata(message);

        this.pong(message);

        try {
            this.mapTrade(message).forEach(this.priceSender::pushPrice);
        } catch (Exception e) {
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
                return;
            }
        } else {
            var inflater = new Inflater();
            inflater.setInput(data);

            var outputStream = new ByteArrayOutputStream();
            var buffer = new byte[1024];

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

    protected void pong(String message) {

    }

    protected void prepareConnection() {
    }

    protected void sendMessage(String message) {

        if (userSession == null) {
            return;
        }

        log.debug("Sending message to {}, payload : {}", getExchange(), message);

        this.userSession.getAsyncRemote().sendText(message);
    }

    protected List<Trade> mapTrade(String message) throws JsonProcessingException {
        return new ArrayList<>();
    }

    protected List<Trade> mapTrade(ByteBuffer message) throws JsonProcessingException {
        return new ArrayList<>();
    }

    protected void decodeMetadata(String message) {
    }


    protected abstract String getUri();

    protected abstract void subscribe();

    protected abstract String getExchange();

    protected void connect() {
        prepareConnection();

        try {
            var container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, new URI(getUri()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected long getTimeout() {
        return DEFAULT_TIMEOUT;
    }

    protected List<String> getAllQuotes(boolean upperCase) {
        return upperCase ? Constants.USD_USDT_USDC_BUSD.stream().map(String::toUpperCase).collect(Collectors.toList())
                : Constants.USD_USDT_USDC_BUSD;
    }

    protected List<String> getAllQuotesExceptBusd(boolean upperCase) {
        return upperCase ? Constants.USD_USDT_USDC.stream().map(String::toUpperCase).collect(Collectors.toList())
                : Constants.USD_USDT_USDC;
    }

}
