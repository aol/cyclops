package com.oath.cyclops.jackson;

import cyclops.control.LazyEither;
import cyclops.control.LazyEither3;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class LazyEither3Test {

   @Test
  public void left1(){
      assertThat(JacksonUtil.serializeToJson(LazyEither3.left1(10)),equalTo("{\"left1\":10}"));

   }
  @Test
  public void left2(){
    assertThat(JacksonUtil.serializeToJson(LazyEither3.left2(10)),equalTo("{\"left2\":10}"));

  }
  @Test
  public void right(){
    assertThat(JacksonUtil.serializeToJson(LazyEither3.right(10)),equalTo("{\"right\":10}"));

  }
  @Test
  public void roundTripLeft1(){

    String json  =JacksonUtil.serializeToJson(LazyEither3.left1(10));
    System.out.println("Json " +  json);
    LazyEither3<Integer,String,String> des = JacksonUtil.convertFromJson(json,LazyEither3.class);

    assertThat(des,equalTo(LazyEither3.left1(10)));
  }
  @Test
  public void roundTripLeft2(){

    String json  =JacksonUtil.serializeToJson(LazyEither3.left2(10));
    System.out.println("Json " +  json);
    LazyEither3<Integer,String,String> des = JacksonUtil.convertFromJson(json,LazyEither3.class);

    assertThat(des,equalTo(LazyEither3.left2(10)));
  }

  @Test
  public void roundTripRight(){

    String json  =JacksonUtil.serializeToJson(LazyEither3.right(10));
    System.out.println("Json " +  json);
    LazyEither3<String,Integer,Integer> des = JacksonUtil.convertFromJson(json,LazyEither3.class);

    assertThat(des,equalTo(LazyEither3.right(10)));
  }

}
