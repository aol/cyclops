package com.aol.cyclops2.matching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.aol.cyclops2.matching.Case.Case2;

import cyclops.data.tuple.Tuple2;
import org.junit.Test;

public class Case2Test {

  @Test
  public void shouldMatchForAllPredicates() {
    Tuple2<String, Integer> tuple2 = new Tuple2<>("tuple", 2);
    assertEquals("tuple2", new Case2<>((String t1) -> t1.equals("tuple"), (Integer t2) -> t2.equals(2), __ -> "tuple2").test(tuple2)
            .orElse(null));
  }

  @Test
  public void shouldMatchForPartial() {
    Tuple2<String, Integer> tuple2 = new Tuple2<>("tuple", 2);
    assertFalse(new Case2<>((String t1) -> t1.equals("tuple"), (Integer t2) -> false, __ -> "tuple2").test(tuple2).isPresent());
    assertFalse(new Case2<>((String t1) -> false, (Integer t2) -> t2.equals(2), __ -> "tuple2").test(tuple2).isPresent());
    assertFalse(new Case2<>((String t1) -> false, (Integer t2) -> false, __ -> "tuple2").test(tuple2).isPresent());
  }


}