package com.oath.cyclops.jackson.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.oath.cyclops.matching.Sealed4;
import com.oath.cyclops.matching.Sealed5;
import com.oath.cyclops.util.ExceptionSoftener;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

public class Sealed5Serializer extends JsonSerializer<Sealed5<?,?,?,?,?>> {

  private static final long serialVersionUID = 1L;


  @AllArgsConstructor
  public static class Left1Bean {
    @Getter
    @Setter
    private final Object left1;
  }
  @AllArgsConstructor
  public static class Left2Bean {
    @Getter
    @Setter
    private final Object left2;
  }
  @AllArgsConstructor
  public static class Left3Bean {
    @Getter
    @Setter
    private final Object left3;
  }
  @AllArgsConstructor
  public static class Left4Bean {
    @Getter
    @Setter
    private final Object left4;
  }

  @AllArgsConstructor
  public static class RightBean {
    @Getter @Setter
    private final Object right;

  }
  @Override
  public void serialize(Sealed5<?,?,?,?,?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

    value.fold( ExceptionSoftener.softenFunction(l1->{
      JsonSerializer<Object> ser = serializers.findValueSerializer(Left1Bean.class);
      ser.serialize(new Left1Bean(l1), gen, serializers);
      return null;
    }),ExceptionSoftener.softenFunction(l2->{
      JsonSerializer<Object> ser = serializers.findValueSerializer(Left2Bean.class);
      ser.serialize(new Left2Bean(l2), gen, serializers);
      return null;
    }),ExceptionSoftener.softenFunction(l3->{
      JsonSerializer<Object> ser = serializers.findValueSerializer(Left3Bean.class);
      ser.serialize(new Left3Bean(l3), gen, serializers);
      return null;
    }),ExceptionSoftener.softenFunction(l4->{
      JsonSerializer<Object> ser = serializers.findValueSerializer(Left4Bean.class);
      ser.serialize(new Left4Bean(l4), gen, serializers);
      return null;
    }),ExceptionSoftener.softenFunction(r->{
      JsonSerializer<Object> ser = serializers.findValueSerializer(RightBean.class);
      ser.serialize(new RightBean(r), gen, serializers);
      return null;
    }));

  }
}
