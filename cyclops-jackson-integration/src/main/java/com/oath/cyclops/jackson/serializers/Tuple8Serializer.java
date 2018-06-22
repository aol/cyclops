package com.oath.cyclops.jackson.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import cyclops.data.tuple.Tuple7;
import cyclops.data.tuple.Tuple8;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

public class Tuple8Serializer extends JsonSerializer<Tuple8<?,?,?,?,?,?,?,?>> {

  private static final long serialVersionUID = 1L;



  @Override
  public void serialize(Tuple8<?,?,?,?,?,?,?,?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

    Object[] array = new Object[]{value._1(),value._2(),value._3(),value._4(),value._5(),value._6(),value._7(),value._8()};
    gen.writeStartArray();
    for(Object o : array) {
      JsonSerializer<Object> ser = serializers.findValueSerializer(o.getClass());
      ser.serialize(o, gen, serializers);
    }
    gen.writeEndArray();
  }
}
