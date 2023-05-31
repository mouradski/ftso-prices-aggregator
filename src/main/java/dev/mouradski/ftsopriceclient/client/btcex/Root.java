package dev.mouradski.ftsopriceclient.client.btcex;

import lombok.Data;

@Data
public class Root {
    private String jsonrpc;
    private String method;
    private Params params;
}
