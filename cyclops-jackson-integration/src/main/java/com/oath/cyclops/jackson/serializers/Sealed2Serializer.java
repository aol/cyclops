package com.oath.cyclops.jackson.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.ReferenceTypeSerializer;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.oath.cyclops.matching.Sealed2;
import com.oath.cyclops.util.ExceptionSoftener;
import cyclops.control.Either;
import cyclops.control.Option;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

public class Sealed2Serializer extends JsonSerializer<Sealed2<?,?>> {

  private static final long serialVersionUID = 1L;


  @AllArgsConstructor
  public static class LeftBean {
    @Getter
    @Setter
    private final Object left;
  }
  @AllArgsConstructor
  public static class RightBean {
    @Getter @Setter
    private final Object right;

  }
  @Override
  public void serialize(Sealed2<?, ?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

    value.fold( ExceptionSoftener.softenFunction(l->{
      JsonSerializer<Object> ser = serializers.findValueSerializer(LeftBean.class);
      ser.serialize(new LeftBean(l), gen, serializers);
      return null;
    }),ExceptionSoftener.softenFunction(r->{
      JsonSerializer<Object> ser = serializers.findValueSerializer(RightBean.class);
      ser.serialize(new RightBean(r), gen, serializers);
      return null;
    }));

  }
}
