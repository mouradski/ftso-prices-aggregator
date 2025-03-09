package dev.mouradski.ftso.prices.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.service.TickerService;
import dev.mouradski.ftso.prices.utils.Constants;
import io.quarkus.scheduler.Scheduled;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.EventCountCircuitBreaker;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

import static dev.mouradski.ftso.prices.utils.Constants.SYMBOLS;

@Slf4j
public abstract class AbstractClientEndpoint {
    public boolean enabled = false;

    @Inject
    TickerService tickerService;

    @ConfigProperty(name = "assets")
    List<String> assets;

    @ConfigProperty(name = "exchanges")
    protected List<String> exchanges;

    public static final Gson gson = new Gson();

    @Inject
    @ConfigProperty(name = "default_message_timeout", defaultValue = "20")
    Long defaultTimeoutInSeconds;

    protected HttpClient client = HttpClient.newHttpClient();

    protected Set<String> symbols;

    protected EventCountCircuitBreaker restCircuitBreaker = new EventCountCircuitBreaker(5, 10, TimeUnit.SECONDS);

    private Long timeout;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected Session userSession = null;
    protected AtomicInteger counter = new AtomicInteger();
    private boolean started = false;


    private long lastMessageTimestamp = OffsetDateTime.now().toEpochSecond();

    protected Set<String> getSymbols(boolean upperCase, String separator) {
        if (symbols == null) {
            symbols = new HashSet<>();
            getAssets(upperCase).forEach(base -> {
                getAllQuotes(upperCase).forEach(quote -> {
                    symbols.add(base + separator + quote);
                });
            });
        }

        return symbols;
    }

    protected AbstractClientEndpoint() {
        if (getUri() == null) {
            try {
                prepareConnection();
            } catch (Exception e) {
                log.error("Error initializing client for {}", getExchange(), e);
            }
        }
    }

    @OnOpen
    public void onOpen(Session userSession) {
        userSession.setMaxTextMessageBufferSize(1024 * 1024 * 10);
        this.userSession = userSession;
        subscribe();
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        this.tickerService.pushError(this.getExchange());
        log.info("Closing websocket for {}, Reason : {}", getExchange(), reason.getReasonPhrase());

        try {
            this.userSession = null;
            Thread.sleep(100);
        } catch (Exception e) {
        }

        connect();
    }

    @OnMessage
    public void onMessage(String message) throws JsonProcessingException {
        try {
            this.decodeMetadata(message);

            if (!this.pong(message)) {
                this.mapTicker(message).ifPresent(tickerList -> tickerList.forEach(this::pushTicker));
            }

        } catch (Exception e) {
            log.debug("Caught exception receiving msg from {}, msg : {}", getExchange(), message, e);
        }

    }

    @Scheduled(every = "5s")
    public void checkMessageReceivedTimeout() {
        if (this.getUri() == null) {
            return;
        }

        if ((currentTimestamp() - this.lastMessageTimestamp) > getTimeout() * 1000) {
            log.error("No data received in a while from {}, reconnecting", getExchange());
            var closeReason = new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "No data received in a while");
            try {
                this.userSession.close(closeReason);
            } catch (IOException e) {
                log.error("Error closing websocket for {}", getExchange(), e);
                onClose(userSession, closeReason);
            }
        }

    }

    protected void pushTicker(Ticker ticker) {
        this.lastMessageTimestamp = currentTimestamp();
        this.tickerService.pushTicker(ticker);
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
        this.tickerService.pushError(this.getExchange());
        log.error("Error from {} : {}", getExchange(), t.getMessage());
    }

    protected boolean pong(String message) {
        var lcMessage = message.toLowerCase();
        return lcMessage.length() < 100 && (lcMessage.contains("ping") || lcMessage.contains("pong"));
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

    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        return Optional.empty();
    }

    protected void decodeMetadata(String message) {
    }

    protected abstract String getUri();

    protected void subscribe() {
        subscribeTicker();
    }

    protected void subscribeTicker() {
    }

    protected abstract String getExchange();

    @PostConstruct
    public void start() {
        if (exchanges == null || exchanges.contains("all") || exchanges.contains(getExchange())) {
            this.enabled = true;

            if (this.getUri() != null) {
                this.connect();
            }
        }
    }

    public synchronized boolean connect() {
        if (getUri() == null) {
            return true;
        }

        if (!started) {
            Timer timer = new Timer();
            TimerTask repeatedTask = new TimerTask() {
                public void run() {
                    checkMessageReceivedTimeout();
                }
            };


            timer.scheduleAtFixedRate(repeatedTask, 10000, getTimeout() * 1000);

            this.started = true;
        }


        if (this.userSession == null || !this.userSession.isOpen()) {

            log.info("Connecting to {} ....", getExchange());

            prepareConnection();

            try {
                var container = ContainerProvider.getWebSocketContainer();

                Config config = ConfigProvider.getConfig();
                Optional<String> uriProperty = config.getOptionalValue(getExchange() + ".ws.uri", String.class);

                container.connectToServer(this, new URI(uriProperty.orElse(getUri())));

                log.info("Connected to {}", getExchange());
                return true;
            } catch (Exception e) {
                this.tickerService.pushError(this.getExchange());
                var reconnectWaitTimeSeconds = 20;
                log.error("Unable to connect to {}, waiting {} seconds to try again", getExchange(), reconnectWaitTimeSeconds);

                var executor = Executors.newSingleThreadScheduledExecutor();
                executor.schedule(this::connect, reconnectWaitTimeSeconds, TimeUnit.SECONDS);
            }
        }

        return false;
    }

    protected long getTimeout() {
        return timeout == null ? defaultTimeoutInSeconds : timeout;
    }

    protected List<String> getAssets() {
        return getAssets(false);
    }

    protected List<String> getAssets(boolean upperCase) {
        List<String> calculatedAssets = assets == null ? SYMBOLS : assets;

        return upperCase ? calculatedAssets.stream().map(String::toUpperCase).toList() : calculatedAssets;
    }

    protected List<String> getAllQuotes(boolean upperCase) {
        return upperCase ? Constants.ALL_QUOTES.stream().map(String::toUpperCase).toList() : Constants.ALL_QUOTES;
    }


    protected List<String> getAllStablecoinQuotes(boolean upperCase) {
        return upperCase ? Constants.USDT_USDC_DAI.stream().map(String::toUpperCase).toList() : Constants.USDT_USDC_DAI;
    }

    protected List<String> getAllStablecoinQuotesExceptBusd(boolean upperCase) {
        return upperCase ? Constants.USDT_USDC_DAI.stream().map(String::toUpperCase).toList() : Constants.USDT_USDC_DAI;
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

    protected void catchRestError(Throwable throwable) {

        if (!this.restCircuitBreaker.incrementAndCheckState()) {
            var executor = Executors.newSingleThreadScheduledExecutor();
            executor.schedule(this.restCircuitBreaker::close, 10, TimeUnit.SECONDS);
        }
        this.tickerService.pushError(this.getExchange());
    }

    protected boolean isCircuitClosed() {
        return restCircuitBreaker.checkState();
    }


    public void setExchanges(List<String> exchanges) {
        this.exchanges = exchanges;
    }

}
