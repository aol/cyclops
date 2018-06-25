package com.oath.cyclops.jackson;

import cyclops.control.Maybe;
import org.junit.Test;



import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class MaybeTest {
   Maybe<Integer> some = Maybe.just(10);

   @Test
  public void roundTrip(){

     String json  =JacksonUtil.serializeToJson(Maybe.of(10));
     System.out.println("Json " +  json);
     Maybe<Integer> des = JacksonUtil.convertFromJson(json,Maybe.class);

     assertThat(des,equalTo(some));
   }

   @Test
   public void nothing(){
     assertThat(JacksonUtil.serializeToJson(Maybe.nothing()),equalTo("null"));
   }
  @Test
  public void just(){
    assertThat(JacksonUtil.serializeToJson(Maybe.just(5)),equalTo("5"));
  }
  @Test
  public void roundTripNull() {

    String json = JacksonUtil.serializeToJson(Maybe.nothing());
    System.out.println("Json " + json);
    Maybe<Integer> des = JacksonUtil.convertFromJson(json, Maybe.class);

    assertThat(des, equalTo(Maybe.nothing()));
  }

}
