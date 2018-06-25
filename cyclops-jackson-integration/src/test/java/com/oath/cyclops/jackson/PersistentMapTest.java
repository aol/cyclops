package com.oath.cyclops.jackson;

import cyclops.data.HashMap;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class PersistentMapTest {

  @Test
  public void hashMap(){
    System.out.println(JacksonUtil.serializeToJson(HashMap.of("a",10)));
    System.out.println(JacksonUtil.serializeToJson(HashMap.empty()));

    assertThat(JacksonUtil.convertFromJson(JacksonUtil.serializeToJson(HashMap.of("a",10)),HashMap.class),equalTo(HashMap.of("a",10)));
  }
}
