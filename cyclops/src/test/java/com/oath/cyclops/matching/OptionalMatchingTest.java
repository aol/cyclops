package com.oath.cyclops.matching;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

public class OptionalMatchingTest {

  @Test
  public void shouldMatchOptionalPresent() {
    Assert.assertEquals((Long) 1L, new Matching.OptionalMatching<>(of("present")).of(new Case.CaseOptional<>(() -> 1L, () -> 2L)));
  }

  @Test
  public void shouldMatchOptionalAbsent() {
    Assert.assertEquals((Long) 2L, new Matching.OptionalMatching<>(empty()).of(new Case.CaseOptional<>(() -> 1L, () -> 2L)));
  }

}
