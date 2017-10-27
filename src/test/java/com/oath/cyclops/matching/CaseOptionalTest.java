package com.oath.cyclops.matching;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import org.junit.Assert;
import org.junit.Test;

public class CaseOptionalTest {

  @Test
  public void shouldMatchOptionalPresent() {
    Case.CaseOptional<String, Long> caseOptional = new Case.CaseOptional<>(() -> 1L, () -> 2L);
    Assert.assertEquals((Long) 1L, caseOptional.test(of("")).orElse(null));
  }

  @Test
  public void shouldMatchOptionalAbsent() {
    Case.CaseOptional<String, Long> caseOptional = new Case.CaseOptional<>(() -> 1L, () -> 2L);
    Assert.assertEquals((Long) 2L, caseOptional.test(empty()).orElse(null));
  }

}
