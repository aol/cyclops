package com.oath.cyclops.jackson;

import cyclops.control.Either;
import cyclops.control.Eval;
import cyclops.control.Option;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class EitherTest {

   @Test
  public void left(){
      assertThat(JacksonUtil.serializeToJson(Either.left(10)),equalTo("{\"left\":10}"));

   }

  @Test
  public void right(){
    assertThat(JacksonUtil.serializeToJson(Either.right(10)),equalTo("{\"right\":10}"));

  }
  @Test
  public void roundTripLeft(){

    String json  =JacksonUtil.serializeToJson(Either.left(10));
    System.out.println("Json " +  json);
    Either<Integer,String> des = JacksonUtil.convertFromJson(json,Either.class);

    assertThat(des,equalTo(Either.left(10)));
  }

  @Test
  public void roundTripRight(){

    String json  =JacksonUtil.serializeToJson(Either.right(10));
    System.out.println("Json " +  json);
    Either<String,Integer> des = JacksonUtil.convertFromJson(json,Either.class);

    assertThat(des,equalTo(Either.right(10)));
  }

}
