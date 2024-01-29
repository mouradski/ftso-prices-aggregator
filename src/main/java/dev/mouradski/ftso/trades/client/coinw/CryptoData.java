package dev.mouradski.ftso.trades.client.coinw;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CryptoData {
    private String percentChange;
    private String high24hr;
    private String last;
    private String highestBid;
    private int id;
    private int isFrozen;
    private String baseVolume;
    private String lowestAsk;
    private String low24hr;
}
