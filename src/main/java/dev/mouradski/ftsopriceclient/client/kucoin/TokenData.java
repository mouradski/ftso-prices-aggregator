package dev.mouradski.ftsopriceclient.client.kucoin;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
class TokenData {
    String token;
    List<InstanceServer> instanceServers;
}
