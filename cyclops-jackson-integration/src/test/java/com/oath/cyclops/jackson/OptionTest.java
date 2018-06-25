package com.oath.cyclops.jackson;

import com.fasterxml.jackson.databind.JavaType;
import cyclops.control.Option;
import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class OptionTest {
   Option<Integer> some = Option.some(10);

   @Test
  public void roundTrip(){

     String json  =JacksonUtil.serializeToJson(Option.of(10));
     System.out.println("Json " +  json);
     Option<Integer> des = JacksonUtil.convertFromJson(json,Option.class);

     assertThat(des,equalTo(some));
   }

   @Test
   public void none(){
     assertThat(JacksonUtil.serializeToJson(Option.none()),equalTo("null"));
   }
  @Test
  public void some(){
    assertThat(JacksonUtil.serializeToJson(Option.some(5)),equalTo("5"));
  }
  @Test
  public void roundTripNull(){

    String json  =JacksonUtil.serializeToJson(Option.none());
    System.out.println("Json " +  json);
    Option<Integer> des = JacksonUtil.convertFromJson(json,Option.class);

    assertThat(des,equalTo(Option.none()));
  }
  @Test
  public void roundTripOptional(){

    String json  =JacksonUtil.serializeToJson(Optional.of(10));
    System.out.println("Json " +  json);
    Optional<Integer> des = JacksonUtil.convertFromJson(json,Optional.class);

    assertThat(des,equalTo(Optional.of(10)));
  }

}
