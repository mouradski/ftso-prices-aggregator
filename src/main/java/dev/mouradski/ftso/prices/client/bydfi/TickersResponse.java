package dev.mouradski.ftso.prices.client.bydfi;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class TickersResponse {
    private int code;
    private Map<String, TickerBody> data;
    private String message;
}

@Getter
@Setter
class TickerBody {
    private String base_id;
    private double base_volume; // Assuming this is a double
    private int isFrozen;       // Assuming this is an int
    private double last_price;  // Assuming this is a double
    private String quote_id;
    private double quote_volume; // Assuming this is a double
}
