package org.ej.docdrop.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.Instant;

public class InstantToStringEpochSerializer extends JsonSerializer<Instant> {

    @Override
    public void serialize(Instant value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        long epochSecond = value.getEpochSecond();
        int nano = value.getNano();

        int milli = nano / 1000000;
        long epochMillisecond = (epochSecond * 1000) + milli;

        gen.writeString(Long.toString(epochMillisecond));
    }
}