package dev.mouradski.ftso.prices.client.deepcoin;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TickersResponse {
    private String code;
    private String msg;
    private List<CryptoDataItem> data;

}
