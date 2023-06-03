package dev.mouradski.prices.client.bitget;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonDeserialize(using = TradeUpdateDataDeserializer.class)
@AllArgsConstructor
@NoArgsConstructor
public class TradeUpdateData {
    private Long timestamp;
    private Double price;
    private Double quantity;
    private String type;
}




