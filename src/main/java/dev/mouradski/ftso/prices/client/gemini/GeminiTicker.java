package dev.mouradski.ftso.prices.client.gemini;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeminiTicker {
    private String symbol;
    private Double close;
}
