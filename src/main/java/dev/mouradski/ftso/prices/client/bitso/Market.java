package dev.mouradski.ftso.prices.client.bitso;

import lombok.Getter;

import java.util.List;

@Getter
public class Market {
    private boolean success;
    private List<PayloadItem> payload;
}
