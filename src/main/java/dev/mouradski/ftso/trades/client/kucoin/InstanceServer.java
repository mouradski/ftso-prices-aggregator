package dev.mouradski.ftso.trades.client.kucoin;

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
