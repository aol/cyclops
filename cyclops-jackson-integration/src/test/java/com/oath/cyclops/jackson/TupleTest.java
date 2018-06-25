package com.oath.cyclops.jackson;

import cyclops.data.Seq;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple1;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class TupleTest {

  @Test
  public void t1(){
    assertThat(JacksonUtil.serializeToJson(Tuple.tuple("hello")),equalTo("[\"hello\"]"));

    assertThat(JacksonUtil.convertFromJson(JacksonUtil.serializeToJson(Tuple.tuple("hello")),Tuple1.class),equalTo(Tuple.tuple("hello")));
  }
  @Test
  public void t2(){
    assertThat(JacksonUtil.serializeToJson(Tuple.tuple("hello","world")),equalTo("[\"hello\",\"world\"]"));
  }
  @Test
  public void t3(){
    assertThat(JacksonUtil.serializeToJson(Tuple.tuple("hello","world","x")),equalTo( "[\"hello\",\"world\",\"x\"]"));
  }
  @Test
  public void t4(){
    assertThat(JacksonUtil.serializeToJson(Tuple.tuple("hello","world","x","a")),equalTo("[\"hello\",\"world\",\"x\",\"a\"]"));
  }
  @Test
  public void t5(){
    assertThat(JacksonUtil.serializeToJson(Tuple.tuple("hello","world","x","a","b")),equalTo("[\"hello\",\"world\",\"x\",\"a\",\"b\"]"));
  }

  @Test
  public void t6(){
    assertThat(JacksonUtil.serializeToJson(Tuple.tuple("hello","world","x","a","b","c")),equalTo("[\"hello\",\"world\",\"x\",\"a\",\"b\",\"c\"]"));
  }
  @Test
  public void t7(){
    assertThat(JacksonUtil.serializeToJson(Tuple.tuple("hello","world","x","a","b","c","d")),equalTo( "[\"hello\",\"world\",\"x\",\"a\",\"b\",\"c\",\"d\"]"));
  }
  @Test
  public void t8(){
    assertThat(JacksonUtil.serializeToJson(Tuple.tuple("hello","world","x","a","b","c","d","e")),equalTo("[\"hello\",\"world\",\"x\",\"a\",\"b\",\"c\",\"d\",\"e\"]"));
  }

  @Test
  public void vec(){
    System.out.println(JacksonUtil.serializeToJson(Seq.of("hello")));
  }


}
