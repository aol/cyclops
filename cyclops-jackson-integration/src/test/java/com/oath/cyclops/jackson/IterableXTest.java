package com.oath.cyclops.jackson;

import cyclops.data.LazySeq;
import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.reactive.ReactiveSeq;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class IterableXTest {
  @Test
  public void seq(){
    System.out.println(JacksonUtil.serializeToJson(Seq.of(1,2,3)));
    Seq<Integer> s = JacksonUtil.convertFromJson(JacksonUtil.serializeToJson(Seq.of(1,2,3)),Seq.class);
    assertThat(s,equalTo(Seq.of(1,2,3)));
  }
  @Test
  public void lazySeq(){
    System.out.println(JacksonUtil.serializeToJson(LazySeq.of(1,2,3)));
    LazySeq<Integer> s = JacksonUtil.convertFromJson(JacksonUtil.serializeToJson(LazySeq.of(1,2,3)),LazySeq.class);
    assertThat(s,equalTo(LazySeq.of(1,2,3)));
  }
  @Test
  public void vector(){
    System.out.println(JacksonUtil.serializeToJson(Vector.of(1,2,3)));
    Vector<Integer> s = JacksonUtil.convertFromJson(JacksonUtil.serializeToJson(Vector.of(1,2,3)),Vector.class);
    assertThat(s,equalTo(Vector.of(1,2,3)));
  }

  @Test
  public void reactiveSeq(){
    System.out.println(JacksonUtil.serializeToJson(ReactiveSeq.of(1,2,3)));
    ReactiveSeq<Integer> s = JacksonUtil.convertFromJson(JacksonUtil.serializeToJson(ReactiveSeq.of(1,2,3)),ReactiveSeq.class);
    assertThat(s.toList(),equalTo(ReactiveSeq.of(1,2,3).toList()));
  }


}
