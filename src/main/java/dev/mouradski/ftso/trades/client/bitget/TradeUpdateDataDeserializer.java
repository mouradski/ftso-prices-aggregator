package dev.mouradski.ftso.trades.client.bitget;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class TradeUpdateDataDeserializer extends StdDeserializer<TradeUpdateData> {

    public TradeUpdateDataDeserializer() {
        this(null);
    }

    public TradeUpdateDataDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public TradeUpdateData deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        long timestamp = node.get(0).asLong();
        double price = node.get(1).asDouble();
        double quantity = node.get(2).asDouble();
        String type = node.get(3).asText();

        return new TradeUpdateData(timestamp, price, quantity, type);
    }
}
