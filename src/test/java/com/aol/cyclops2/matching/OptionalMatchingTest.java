package com.aol.cyclops2.matching;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.Assert.assertEquals;

import com.aol.cyclops2.matching.Case.CaseOptional;
import com.aol.cyclops2.matching.Matching.OptionalMatching;

import org.junit.Test;

public class OptionalMatchingTest {

  @Test
  public void shouldMatchOptionalPresent() {
    assertEquals((Long) 1L, new OptionalMatching<>(of("present")).of(new CaseOptional<>(() -> 1L, () -> 2L)));
  }

  @Test
  public void shouldMatchOptionalAbsent() {
    assertEquals((Long) 2L, new OptionalMatching<>(empty()).of(new CaseOptional<>(() -> 1L, () -> 2L)));
  }

}