package dev.mouradski.ftso.trades.service;

import dev.mouradski.ftso.trades.client.HttpTickers;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
@Slf4j
public class HttpTickersUpdaterService {

    @Inject
    Instance<HttpTickers> httpTickers;

    private ExecutorService executorService = Executors.newFixedThreadPool(10); // N est le nombre de threads


    public void update() {
        httpTickers.forEach(httpTicker -> {
            try {
            executorService.submit(httpTicker::updateTickers);
            } catch (Exception ignored) {
            }
        });
    }
}
