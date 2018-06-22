package com.oath.cyclops.jackson;

import cyclops.control.LazyEither4;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Ignore
public class LazyEither4Test {

   @Test
  public void left1(){
      assertThat(JacksonUtil.serializeToJson(LazyEither4.left1(10)),equalTo("{\"left1\":10}"));

   }
  @Test
  public void left2(){
    assertThat(JacksonUtil.serializeToJson(LazyEither4.left2(10)),equalTo("{\"left2\":10}"));

  }
  @Test
  public void left3(){
    assertThat(JacksonUtil.serializeToJson(LazyEither4.left3(10)),equalTo("{\"left3\":10}"));

  }
  @Test
  public void right(){
    assertThat(JacksonUtil.serializeToJson(LazyEither4.right(10)),equalTo("{\"right\":10}"));

  }
  @Test
  public void roundTripLeft1(){

    String json  =JacksonUtil.serializeToJson(LazyEither4.left1(10));
    System.out.println("Json " +  json);
    LazyEither4<Integer,String,String,String> des = JacksonUtil.convertFromJson(json,LazyEither4.class);

    assertThat(des,equalTo(LazyEither4.left1(10)));
  }
  @Test
  public void roundTripLeft2(){

    String json  =JacksonUtil.serializeToJson(LazyEither4.left2(10));
    System.out.println("Json " +  json);
    LazyEither4<Integer,String,String,String> des = JacksonUtil.convertFromJson(json,LazyEither4.class);

    assertThat(des,equalTo(LazyEither4.left2(10)));
  }
  @Test
  public void roundTripLeft3(){

    String json  =JacksonUtil.serializeToJson(LazyEither4.left3(10));
    System.out.println("Json " +  json);
    LazyEither4<Integer,String,String,String> des = JacksonUtil.convertFromJson(json,LazyEither4.class);

    assertThat(des,equalTo(LazyEither4.left3(10)));
  }

  @Test
  public void roundTripRight(){

    String json  =JacksonUtil.serializeToJson(LazyEither4.right(10));
    System.out.println("Json " +  json);
    LazyEither4<String,Integer,Integer,Integer> des = JacksonUtil.convertFromJson(json,LazyEither4.class);

    assertThat(des,equalTo(LazyEither4.right(10)));
  }

}
