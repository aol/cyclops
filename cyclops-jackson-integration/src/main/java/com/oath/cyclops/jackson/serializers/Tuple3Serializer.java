package com.oath.cyclops.jackson.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

public class Tuple3Serializer extends JsonSerializer<Tuple3<?,?,?>> {

  private static final long serialVersionUID = 1L;



  @Override
  public void serialize(Tuple3<?,?,?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

    Object[] array = new Object[]{value._1(),value._2(),value._3()};
    gen.writeStartArray();
    for(Object o : array) {
      JsonSerializer<Object> ser = serializers.findTypedValueSerializer(o.getClass(),true,null);
      ser.serialize(o, gen, serializers);
    }
    gen.writeEndArray();

  }
}
