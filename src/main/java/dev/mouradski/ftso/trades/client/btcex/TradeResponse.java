package dev.mouradski.ftso.trades.client.btcex;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class TradeResponse {
    String jsonrpc;
    String method;
    Params params;
}
