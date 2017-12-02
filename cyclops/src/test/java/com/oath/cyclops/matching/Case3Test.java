package com.oath.cyclops.matching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import cyclops.data.tuple.Tuple3;
import org.junit.Assert;
import org.junit.Test;

public class Case3Test {

  @Test
  public void shouldMatchForAllPredicates() {
    Tuple3<String, Integer, String> tuple3 = new Tuple3<>("tuple", 2, "hello_world");
    Assert.assertEquals("tuple3", new Case.Case3<>((String t1) -> t1.equals("tuple"), (Integer t2) -> t2.equals(2), (String t3) -> t3.equals("hello_world"), () -> "tuple3").test(tuple3)
            .orElse(null));
  }

  @Test
  public void shouldNotMatchForPartial() {
    Tuple3<String, Integer, String> tuple2 = new Tuple3<>("tuple", 2, "hello_world");
    assertFalse(new Case.Case3<>((String t1) -> t1.equals("tuple"), (Integer t2) -> t2.equals(2), (String t3) -> false, () -> "tuple2").test(tuple2).isPresent());
    assertFalse(new Case.Case3<>((String t1) -> t1.equals("tuple"), (Integer t2) -> false, (String t3) -> t3.equals("hello_word"), () -> "tuple2").test(tuple2).isPresent());
    assertFalse(new Case.Case3<>((String t1) -> false, (Integer t2) -> t2.equals(2), (String t3) -> t3.equals("hello_word"), () -> "tuple2").test(tuple2).isPresent());
  }


}
