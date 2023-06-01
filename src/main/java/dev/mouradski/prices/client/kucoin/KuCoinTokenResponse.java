package dev.mouradski.prices.client.kucoin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
class KuCoinTokenResponse {
    String code;
    TokenData data;
}
