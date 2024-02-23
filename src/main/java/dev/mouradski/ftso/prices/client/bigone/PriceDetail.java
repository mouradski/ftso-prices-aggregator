package dev.mouradski.ftso.prices.client.bigone;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class PriceDetail {
    public String price;
    @JsonProperty("order_count")
    public int orderCount;
    public String quantity;
}
