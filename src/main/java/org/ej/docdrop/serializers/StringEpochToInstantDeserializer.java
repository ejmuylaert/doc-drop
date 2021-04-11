package org.ej.docdrop.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;

import java.io.IOException;
import java.time.Instant;

public class StringEpochToInstantDeserializer extends StdDeserializer<Instant> {
    public StringEpochToInstantDeserializer() {
        this(null);
    }

    public StringEpochToInstantDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Instant deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {

        TextNode node = parser.getCodec().readTree(parser);
        String stringEpoch = node.textValue();
        long epoch = Long.parseLong(stringEpoch);
        return Instant.ofEpochMilli(epoch);
    }
}