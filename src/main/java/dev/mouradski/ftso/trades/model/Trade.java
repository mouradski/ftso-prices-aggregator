package dev.mouradski.ftso.trades.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Trade {
    private String exchange;
    private String base;
    private String quote;
    private Double price;
    private Double amount;
    private Long timestamp;
    private Double volumeWeightByExchangeBaseQuote;
    private Double volumeWeightByExchangeBase;
}
