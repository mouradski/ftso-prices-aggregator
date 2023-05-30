package dev.mouradski.ftsopriceclient.client.bittrex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import dev.mouradski.ftsopriceclient.client.AbstractClientEndpoint;
import dev.mouradski.ftsopriceclient.model.Trade;
import dev.mouradski.ftsopriceclient.service.PriceService;
import jakarta.websocket.ClientEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

//@Component
@ClientEndpoint
@Slf4j
public class BittrexClientEndpoint extends AbstractClientEndpoint {

    private String token;

    protected BittrexClientEndpoint(PriceService priceSender, @Value("${exchanges}") List<String> exchanges, @Value("${assets}") List<String> assets) {
        super(priceSender, exchanges, assets);
    }

    @Override
    protected String getUri() {
        try {
            return "wss://socket-v3.bittrex.com/signalr/connect?clientProtocol=1.5&transport=webSockets&connectionToken=TOKEN&connectionData=DATA&tid=10"
                    .replace("TOKEN",  URLEncoder.encode(token, StandardCharsets.UTF_8.toString())).replace("DATA", URLEncoder.encode("[{\"name\":\"c3\"}]", StandardCharsets.UTF_8.toString()));
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    @Override
    protected void subscribe() {
        getAssets().stream().map(String::toUpperCase).forEach(symbol -> {
            getAllQuotesExceptBusd(true).forEach(quote -> {
                this.sendMessage("{\"H\":\"c3\",\"M\":\"Subscribe\",\"A\":[[\"trade_SYMBOL-QUOTE\"]],\"I\":ID}".replace("ID", counter.getCount() + "").replace("SYMBOL", symbol).replace("QUOTE", quote));
            });
        });
    }

    @Override
    protected String getExchange() {
        return "bittrex";
    }

    @Override
    protected void prepareConnection() {
        try {
            var client = HttpClient.newHttpClient();

            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://socket-v3.bittrex.com/signalr/negotiate?connectionData=" + URLEncoder.encode("[{\"name\":\"c3\"}]", StandardCharsets.UTF_8.toString()) + "&clientProtocol=1.5"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            var gson = new Gson();
            var tokenResponse = gson.fromJson(response.body(), TokenResponse.class);

            this.token = tokenResponse.getConnectionToken();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected List<Trade> mapTrade(String message) throws JsonProcessingException {

        if (message.contains("trade\",\"A\":[\"")) {

            var compressed = message.split("trade\",\"A\":\\[\"")[1].split("\"")[0];

            var bytes = Base64.getDecoder().decode(compressed);



            try {
                var baos = new ByteArrayOutputStream();
                var dos = new DeflaterOutputStream(baos);

                dos.write(bytes);
                dos.flush();
                dos.close();

                var bais = new ByteArrayInputStream(baos.toByteArray());
                var iis = new InflaterInputStream(bais);

                String result = "";
                var buf = new byte[5];
                var rlen = -1;
                while ((rlen = iis.read(buf)) != -1) {
                    result += new String(Arrays.copyOf(buf, rlen));
                }

            } catch (IOException e) {
                log.error("", e);
            }
        }

        return super.mapTrade(message);
    }
}
