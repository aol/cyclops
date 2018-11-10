package com.oath.cyclops.jackson;

import cyclops.control.Either;
import cyclops.control.LazyEither;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class LazyEitherTest {

   @Test
  public void left(){
      assertThat(JacksonUtil.serializeToJson(LazyEither.left(10)),equalTo("{\"left\":10}"));

   }

  @Test
  public void right(){
    assertThat(JacksonUtil.serializeToJson(LazyEither.right(10)),equalTo("{\"right\":10}"));

  }

  @Test
  public void roundTripLeft(){

    String json  =JacksonUtil.serializeToJson(LazyEither.left(10));
    System.out.println("Json " +  json);
    LazyEither<Integer,String> des = JacksonUtil.convertFromJson(json,LazyEither.class);

    assertThat(des,equalTo(LazyEither.left(10)));
  }

  @Test
  public void roundTripRight(){

    String json  =JacksonUtil.serializeToJson(LazyEither.right(10));
    System.out.println("Json " +  json);
    LazyEither<String,Integer> des = JacksonUtil.convertFromJson(json,LazyEither.class);

    assertThat(des,equalTo(LazyEither.right(10)));
  }

}
