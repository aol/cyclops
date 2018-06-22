package com.oath.cyclops.jackson;

import cyclops.control.Ior;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class IorTest {

   @Test
  public void left(){
      assertThat(JacksonUtil.serializeToJson(Ior.left(10)),equalTo("{\"left\":10}"));

   }

  @Test
  public void right(){
    assertThat(JacksonUtil.serializeToJson(Ior.right(10)),equalTo("{\"right\":10}"));

  }
  @Test
  public void both(){
    assertThat(JacksonUtil.serializeToJson(Ior.both("hello",10)),equalTo("{\"left\":\"hello\",\"right\":10}"));

  }
  @Test
  public void roundTripLeft(){

    String json  =JacksonUtil.serializeToJson(Ior.left(10));
    System.out.println("Json " +  json);
    Ior<Integer,String> des = JacksonUtil.convertFromJson(json,Ior.class);

    assertThat(des,equalTo(Ior.left(10)));
  }

  @Test
  public void roundTripRight(){

    String json  =JacksonUtil.serializeToJson(Ior.right(10));
    System.out.println("Json " +  json);
    Ior<String,Integer> des = JacksonUtil.convertFromJson(json,Ior.class);

    assertThat(des,equalTo(Ior.right(10)));
  }

  @Test
  public void roundTripBoth(){

    String json  =JacksonUtil.serializeToJson(Ior.both("hello",10));
    System.out.println("Json " +  json);
    Ior<String,Integer> des = JacksonUtil.convertFromJson(json,Ior.class);

    assertThat(des,equalTo(Ior.both("hello",10)));
  }

}
