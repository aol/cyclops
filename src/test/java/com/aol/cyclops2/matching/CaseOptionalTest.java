package com.aol.cyclops2.matching;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.Assert.*;

import com.aol.cyclops2.matching.Case.CaseOptional;

import org.junit.Test;

public class CaseOptionalTest {

  @Test
  public void shouldMatchOptionalPresent() {
    CaseOptional<String, Long> caseOptional = new CaseOptional<>(() -> 1L, () -> 2L);
    assertEquals((Long) 1L, caseOptional.test(of("")).get());
  }

  @Test
  public void shouldMatchOptionalAbsent() {
    CaseOptional<String, Long> caseOptional = new CaseOptional<>(() -> 1L, () -> 2L);
    assertEquals((Long) 2L, caseOptional.test(empty()).get());
  }

}