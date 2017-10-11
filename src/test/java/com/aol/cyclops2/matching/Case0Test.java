package com.aol.cyclops2.matching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class Case0Test {

  @Test
  public void shouldMatchForAllPredicates() {
    String value = "value";
    assertEquals("case0", new Case.Case0<>((String t1) -> t1.equals("value"), () -> "case0").test(value).get());
  }

  @Test
  public void shouldMatchForPartial() {
    String value = "value";
    assertFalse(new Case.Case0<>((String t1) -> false, () -> "case0").test(value).isPresent());
  }


}