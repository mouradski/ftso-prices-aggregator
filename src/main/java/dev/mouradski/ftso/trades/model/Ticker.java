package dev.mouradski.ftso.trades.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Ticker {
    private Double lastPrice;
    private String exchange;
    private String base;
    private String quote;
    private Long timestamp;
}
