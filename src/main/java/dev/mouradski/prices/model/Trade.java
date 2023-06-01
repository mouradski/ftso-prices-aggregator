package dev.mouradski.prices.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Trade {
    private Double price;
    private String symbol;
    private String quote;
    private Double amount;
    private String exchange;
}
