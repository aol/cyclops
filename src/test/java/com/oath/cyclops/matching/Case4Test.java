package com.oath.cyclops.matching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import cyclops.data.tuple.Tuple4;
import org.junit.Test;

public class Case4Test {

  @Test
  public void shouldMatchForAllPredicates() {
    Tuple4<String, Integer, String, Integer> tuple4 = new Tuple4<>("tuple", 2, "hello_world", 10);
    assertEquals("tuple4", new Case.Case4<>((String t1) -> t1.equals("tuple"), (Integer t2) -> t2.equals(2), (String t3) -> t3.equals("hello_world"), (Integer t4) -> t4.equals(10), () -> "tuple4").test(tuple4).orElse(""));
  }

  @Test
  public void shouldNotMatchForPartial() {
    Tuple4<String, Integer, String, Integer> tuple4 = new Tuple4<>("tuple", 2, "hello_world", 10);
    assertFalse(new Case.Case4<>((String t1) -> t1.equals("tuple"), (Integer t2) -> t2.equals(2), (String t3) -> t3.equals("hello_world"), (Integer t4) -> false, () -> "tuple4").test(tuple4).isPresent());
    assertFalse(new Case.Case4<>((String t1) -> t1.equals("tuple"), (Integer t2) -> t2.equals(2), (String t3) -> false, (Integer t4) -> t4.equals(10), () -> "tuple4").test(tuple4).isPresent());
    assertFalse(new Case.Case4<>((String t1) -> t1.equals("tuple"), (Integer t2) -> false, (String t3) -> t3.equals("hello_world"), (Integer t4) -> t4.equals(10), () -> "tuple4").test(tuple4).isPresent());
    assertFalse(new Case.Case4<>((String t1) -> false, (Integer t2) -> t2.equals(2), (String t3) -> t3.equals("hello_world"), (Integer t4) -> t4.equals(10), () -> "tuple4").test(tuple4).isPresent());
    assertFalse(new Case.Case4<>((String t1) -> false, (Integer t2) -> false, (String t3) -> false, (Integer t4) -> false, () -> "tuple4").test(tuple4).isPresent());
  }


}
