package com.aol.cyclops2.matching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import cyclops.data.tuple.Tuple1;
import org.junit.Test;

public class Case1Test {

  @Test
  public void shouldMatchForAllPredicates() {
    Tuple1<String> tuple1 = new Tuple1<>("tuple");
    assertEquals("tuple1", new Case.Case1<>((String t1) -> t1.equals("tuple"), () -> "tuple1").test(tuple1).orElse(null));
  }

  @Test
  public void shouldMatchForPartial() {
    Tuple1<String> tuple1 = new Tuple1<>("tuple");
    assertFalse(new Case.Case1<>((String t1) -> false, () -> "tuple").test(tuple1).isPresent());
  }


}