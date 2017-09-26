package com.aol.cyclops2.matching;

import static java.util.Arrays.stream;

import com.aol.cyclops2.matching.Case.Any;

import lombok.AllArgsConstructor;

import java.util.Optional;


public interface Matching {

  @AllArgsConstructor
  final class PatternMatching<T> implements Matching {

    private final T value;

    @SafeVarargs
    public final <R> Optional<R> of(Case<T, R>... cases) {
      return stream(cases).reduce(Case::or).flatMap(c -> c.test(value));
    }

    public <R> R of(Case<T, R> case1, Any<R> any) {
      return case1.test(value).orElseGet(any);
    }

    public <R> R of(Case<T, R> case1, Case<T, R> case2, Any<R> any) {
      return case1.or(case2).test(value).orElseGet(any);
    }

    public <R> R of(Case<T, R> case1, Case<T, R> case2, Case<T, R> case3, Any<R> any) {
      return of(case1, case2, case3).orElseGet(any);
    }

    public <R> R of(Case<T, R> case1, Case<T, R> case2, Case<T, R> case3, Case<T, R> case4, Any<R> any) {
      return of(case1, case2, case3, case4).orElseGet(any);
    }

    public <R> R of(Case<T, R> case1, Case<T, R> case2, Case<T, R> case3, Case<T, R> case4, Case<T, R> case5, Any<R> any) {
      return of(case1, case2, case3, case4, case5).orElseGet(any);
    }

    public <R> R of(Case<T, R> case1, Case<T, R> case2, Case<T, R> case3, Case<T, R> case4, Case<T, R> case5, Case<T, R> case6, Any<R> any) {
      return of(case1, case2, case3, case4, case5, case6).orElseGet(any);
    }

    public <R> R of(Case<T, R> case1, Case<T, R> case2, Case<T, R> case3, Case<T, R> case4, Case<T, R> case5, Case<T, R> case6, Case<T, R> case7, Any<R> any) {
      return of(case1, case2, case3, case4, case5, case6, case7).orElseGet(any);
    }

    public <R> R of(Case<T, R> case1, Case<T, R> case2, Case<T, R> case3, Case<T, R> case4, Case<T, R> case5, Case<T, R> case6, Case<T, R> case7, Case<T, R> case8, Any<R> any) {
      return of(case1, case2, case3, case4, case5, case6, case7, case8).orElseGet(any);
    }

  }

  @AllArgsConstructor
  final class OptionalMatching<T> implements Matching {

    private final Optional<T> value;

    public <R> R of(Case.CaseOptional<T, R> caseOptional) {
      return caseOptional.test(value).get();
    }

  }

}
