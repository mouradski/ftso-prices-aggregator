package dev.mouradski.ftso.prices.client.kucoin;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
class TokenData {
    String token;
    List<dev.mouradski.ftso.prices.client.kucoin.InstanceServer> instanceServers;
}
