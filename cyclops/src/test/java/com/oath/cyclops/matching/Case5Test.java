package com.oath.cyclops.matching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import cyclops.data.tuple.Tuple5;
import org.junit.Assert;
import org.junit.Test;

public class Case5Test {

  @Test
  public void shouldMatchForAllPredicates() {
    Tuple5<String, Integer, String, Integer, Boolean> tuple5 = new Tuple5<>("tuple", 2, "hello_world", 10, false);
    Assert.assertEquals("tuple5", new Case.Case5<>((String t1) -> t1.equals("tuple"), (Integer t2) -> t2.equals(2), (String t3) -> t3.equals("hello_world"), (Integer t4) -> t4.equals(10), (Boolean t5) -> t5.equals(false), (value) -> "tuple5").test(tuple5)
            .orElse(null));
  }

  @Test
  public void shouldNotMatchForPartial() {
    Tuple5<String, Integer, String, Integer, Boolean> tuple5 = new Tuple5<>("tuple", 2, "hello_world", 10, false);
    assertFalse(new Case.Case5<>((String t1) -> t1.equals("tuple"), (Integer t2) -> t2.equals(2), (String t3) -> t3.equals("hello_world"), (Integer t4) -> t4.equals(10), (Boolean t5) -> false, (value) -> "tuple5").test(tuple5).isPresent());
    assertFalse(new Case.Case5<>((String t1) -> t1.equals("tuple"), (Integer t2) -> t2.equals(2), (String t3) -> t3.equals("hello_world"), (Integer t4) -> false, (Boolean t5) -> t5.equals(false), (value) -> "tuple5").test(tuple5).isPresent());
    assertFalse(new Case.Case5<>((String t1) -> t1.equals("tuple"), (Integer t2) -> t2.equals(2), (String t3) -> false, (Integer t4) -> t4.equals(10), (Boolean t5) -> t5.equals(false), (value) -> "tuple5").test(tuple5).isPresent());
    assertFalse(new Case.Case5<>((String t1) -> t1.equals("tuple"), (Integer t2) -> false, (String t3) -> t3.equals("hello_world"), (Integer t4) -> t4.equals(10), (Boolean t5) -> t5.equals(false), (value) -> "tuple5").test(tuple5).isPresent());
    assertFalse(new Case.Case5<>((String t1) -> false, (Integer t2) -> t2.equals(2), (String t3) -> t3.equals("hello_world"), (Integer t4) -> t4.equals(10), (Boolean t5) -> t5.equals(false), (value) -> "tuple5").test(tuple5).isPresent());
    assertFalse(new Case.Case5<>((String t1) -> false, (Integer t2) -> false, (String t3) -> false, (Integer t4) -> false, (Boolean t5) -> false, (value) -> "tuple5").test(tuple5).isPresent());
  }


}
