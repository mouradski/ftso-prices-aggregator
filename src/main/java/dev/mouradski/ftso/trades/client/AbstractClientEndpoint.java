package dev.mouradski.ftso.trades.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import dev.mouradski.ftso.trades.model.Ticker;
import dev.mouradski.ftso.trades.model.Trade;
import dev.mouradski.ftso.trades.service.TickerService;
import dev.mouradski.ftso.trades.service.TradeService;
import dev.mouradski.ftso.trades.utils.Constants;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

import static dev.mouradski.ftso.trades.utils.Constants.SYMBOLS;

@Slf4j
public abstract class AbstractClientEndpoint {

    @Inject
    TradeService tradeService;

    @Inject
    TickerService tickerService;

    @ConfigProperty(name = "assets")
    List<String> assets;

    @ConfigProperty(name = "exchanges")
    protected List<String> exchanges;

    public static final Gson gson = new Gson();

    @Inject
    @ConfigProperty(name = "default_message_timeout", defaultValue = "30")
    Long DEFAULT_TIMEOUT_IN_SECONDS;

    private Long timeout;

    private boolean shutdown = false;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected Session userSession = null;
    protected AtomicInteger counter = new AtomicInteger();
    protected long lastTradeTime = System.currentTimeMillis();
    protected long lastTickerTime = System.currentTimeMillis();

    private ScheduledExecutorService timeoutExecutor = Executors.newScheduledThreadPool(2);
    private ScheduledFuture<?> timeoutFuture;

    @ConfigProperty(name = "subscribe.trade")
    protected boolean subscribeTrade;

    @ConfigProperty(name = "subscribe.ticker")
    protected boolean subscribeTicker;

    private volatile int reconnectionAttempts = 0;

    protected AbstractClientEndpoint() {
        Thread shutdownHook = new Thread(() -> {
            log.info("Shutting down {}", getExchange());
            this.shutdown = true;
        });
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @OnOpen
    public void onOpen(Session userSession) {
        userSession.setMaxTextMessageBufferSize(1024 * 1024 * 10);
        this.userSession = userSession;

        subscribe();
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        if (shutdown) {
            return;
        }

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
            this.decodeMetadata(message);

            if (!this.pong(message)) {
                if (subscribeTrade) {
                    this.mapTrade(message).ifPresent(tradeList -> tradeList.forEach(this::pushTrade));
                }
                if (subscribeTicker) {
                    this.mapTicker(message).ifPresent(tickerList -> tickerList.forEach(this::pushTicker));
                }
            }

            if (timeoutFuture != null) {
                timeoutFuture.cancel(true);
            }
            if (timeoutFuture.isCancelled()) {
                timeoutFuture = timeoutExecutor.schedule(this::checkMessageReceivedTimeout, getTimeout(),
                        TimeUnit.SECONDS);
            }

        } catch (Exception e) {
            log.debug("Caught exception receiving msg from {}, msg : {}", getExchange(), message, e);
        }

    }

    private void checkMessageReceivedTimeout() {
        if (subscribeTrade && this.userSession != null && this.userSession.isOpen()
                && System.currentTimeMillis() - lastTradeTime > getTimeout() * 1000) {

            log.info("No trade received from {} for {} seconds. Reconnecting...", getExchange(), getTimeout());

            onClose(userSession,
                    new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "No data received in a while"));
        }

        if (subscribeTicker && !httpTicker() && this.userSession != null && this.userSession.isOpen()
                && System.currentTimeMillis() - lastTickerTime > getTimeout() * 1000) {
            log.info("No ticker received from {} for {} seconds. Reconnecting...", getExchange(), getTimeout());

            onClose(userSession,
                    new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "No data received in a while"));
        }
    }

    private void pushTrade(Trade trade) {
        this.lastTradeTime = System.currentTimeMillis();
        this.tradeService.pushTrade(trade);
    }

    protected void pushTicker(Ticker ticker) {
        this.lastTickerTime = System.currentTimeMillis();
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
        log.error("Error from {} : {}", getExchange(), t.getMessage());
    }

    protected boolean pong(String message) {
        return false;
    }

    protected void prepareConnection() {
    }

    protected boolean httpTicker() {
        return false;
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

    protected Optional<List<Ticker>> mapTicker(String message) throws JsonProcessingException {
        return Optional.empty();
    }

    protected Optional<List<Trade>> mapTrade(ByteBuffer message) throws JsonProcessingException {
        return Optional.empty();
    }

    protected void decodeMetadata(String message) {
    }

    protected abstract String getUri();

    protected void subscribe() {
        if (subscribeTrade) {
            subscribeTrade();
        }

        if (subscribeTicker) {
            subscribeTicker();
        }
    }

    protected void subscribeTrade() {
    }

    protected void subscribeTicker() {
    }

    protected abstract String getExchange();

    @PostConstruct
    protected void start() {
        timeoutFuture = timeoutExecutor.schedule(this::checkMessageReceivedTimeout, getTimeout(),
                TimeUnit.SECONDS);

        if (exchanges == null || exchanges.contains(getExchange())) {
            this.connect();
        }
    }

    public void shutdown() throws IOException {
        shutdown = true;
        this.userSession.close();
    }

    public synchronized boolean connect() {
        if (shutdown) {
            return false;
        }

        if (this.userSession == null || !this.userSession.isOpen()) {
            log.info("Connecting to {} ....", getExchange());

            prepareConnection();

            try {
                var container = ContainerProvider.getWebSocketContainer();

                Config config = ConfigProvider.getConfig();
                Optional<String> uriProperty = config.getOptionalValue(getExchange() + ".ws.uri", String.class);

                container.connectToServer(this, new URI(uriProperty.orElse(getUri())));

                this.reconnectionAttempts = 0;
                log.info("Connected to {}", getExchange());
                return true;
            } catch (Exception e) {
                var reconnectWaitTimeSeconds = getExponentialBackoffTimeSeconds(++reconnectionAttempts);
                log.error("Unable to connect to {}, waiting {} seconds to try again", getExchange(),
                        reconnectWaitTimeSeconds);

                var executor = Executors.newSingleThreadScheduledExecutor();
                executor.schedule(this::connect, reconnectWaitTimeSeconds, TimeUnit.SECONDS);
            }
        }

        return false;
    }

    protected long getTimeout() {
        return timeout == null ? DEFAULT_TIMEOUT_IN_SECONDS : timeout;
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

    protected List<String> getAllStablecoinQuotes(boolean upperCase) {
        return upperCase ? Constants.USDT_USDC_BUSD.stream().map(String::toUpperCase).toList()
                : Constants.USDT_USDC_BUSD;
    }

    protected List<String> getAllStablecoinQuotesExceptBusd(boolean upperCase) {
        return upperCase ? Constants.USDT_USDC.stream().map(String::toUpperCase).toList()
                : Constants.USDT_USDC;
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

    public void setAssets(List<String> assets) {
        this.assets = assets;
    }

    public void setExchanges(List<String> exchanges) {
        this.exchanges = exchanges;
    }

    public void setSubscribeTrade(boolean subscribeTrade) {
        this.subscribeTrade = subscribeTrade;
    }

    public void setSubscribeTicker(boolean subscribeTicker) {
        this.subscribeTicker = subscribeTicker;
    }

    public void setTradeService(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    public void setTickerService(TickerService tickerService) {
        this.tickerService = tickerService;
    }

    /*
     * t = f(x) = round(1.3^x)
     * gives values 1,1,2,2,3,4,5,6,8,11,14,18,23,30,39,51
     * for the first 16 attempts
     * Return a max of 60 seconds
     */
    private int getExponentialBackoffTimeSeconds(int attempt) {
        if (attempt < 0) {
            attempt = 1;
        }

        double BASE = 1.3;

        int result = Math.round((float) Math.pow(BASE, (double) attempt));

        return result > 60 ? 60 : result;
    }
}
