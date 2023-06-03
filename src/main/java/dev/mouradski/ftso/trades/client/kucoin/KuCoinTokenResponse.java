package dev.mouradski.ftso.trades.client.kucoin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
class KuCoinTokenResponse {
    String code;
    dev.mouradski.ftso.trades.client.kucoin.TokenData data;
}
