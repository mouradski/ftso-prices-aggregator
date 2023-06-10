package dev.mouradski.ftso.trades.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Trade {
    private Double price;
    private String base;
    private String quote;
    private Double amount;
    private String exchange;
}
