package com.oath.cyclops.jackson.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class IterableXSerializer extends JsonSerializer<Iterable<?>>{

  @Override
  public void serialize(Iterable<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    if(value==null) {
      gen.writeNull();
      return;
    }
    gen.writeStartArray();
    for(Object o : value) {
      JsonSerializer<Object> ser = serializers.findValueSerializer(o.getClass());
      ser.serialize(o, gen, serializers);
    }
    gen.writeEndArray();
  }
}
