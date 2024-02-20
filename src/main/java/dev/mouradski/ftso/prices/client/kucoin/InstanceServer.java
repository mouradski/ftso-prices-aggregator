package dev.mouradski.ftso.prices.client.kucoin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class InstanceServer {
    String endpoint;
    boolean encrypt;
    String protocol;
    int pingInterval;
    int pingTimeout;
}
