package dev.mouradski.ftso.prices.client.azbit;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.mouradski.ftso.prices.client.AbstractClientEndpoint;
import dev.mouradski.ftso.prices.model.Source;
import dev.mouradski.ftso.prices.model.Ticker;
import dev.mouradski.ftso.prices.utils.SymbolHelper;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

@ApplicationScoped
@Startup
public class AzbitClientEndpoint extends AbstractClientEndpoint {

    @Override
    protected void subscribeTicker() {
        super.subscribeTicker();
    }

    @Override
    protected String getUri() {
        return "wss://ws.azbit.com/latest-price";
    }

    @Override
    protected String getExchange() {
        return "azbit";
    }
}
