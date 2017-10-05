package com.aol.cyclops2.matching;

import static java.util.Arrays.stream;

import com.aol.cyclops2.matching.Case.Any;

import lombok.AllArgsConstructor;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple0;

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
  final class PatternMatching2<T1,T2> implements Matching {

    private final Sealed2<T1,T2> value;

    public <R> R of(Case<T1, R> case1, Case<T2, R> case2) {
      return value.fold(a->case1.test(a), b->case2.test(b)).get();
    }

  }
  @AllArgsConstructor
  final class PatternMatching3<T1,T2,T3> implements Matching {

    private final Sealed3<T1,T2,T3> value;

    public <R> R of(Case<T1, R> case1, Case<T2, R> case2,Case<T3, R> case3) {
      return value.fold(a->case1.test(a), b->case2.test(b), c->case3.test(c)).get();
    }

  }
    @AllArgsConstructor
    final class PatternMatching4<T1,T2,T3,T4> implements Matching {

        private final Sealed4<T1,T2,T3,T4> value;

        public <R> R of(Case<T1, R> case1, Case<T2, R> case2,Case<T3, R> case3,Case<T4, R> case4) {
            return value.fold(a->case1.test(a), b->case2.test(b), c->case3.test(c), d->case4.test(d)).get();
        }

    }
    @AllArgsConstructor
    final class PatternMatching5<T1,T2,T3,T4,T5> implements Matching {

        private final Sealed5<T1,T2,T3,T4,T5> value;

        public <R> R of(Case<T1, R> case1, Case<T2, R> case2,Case<T3, R> case3,Case<T4, R> case4, Case<T5, R> case5) {
            return value.fold(a->case1.test(a), b->case2.test(b), c->case3.test(c), d->case4.test(d), e->case5.test(e)).get();
        }

    }
  @AllArgsConstructor
  final class PatternMatchingOrNone<T1> implements Matching {

    private final Sealed1Or<T1> value;

    public <R> R of(Case<T1, R> case1, Case<Tuple0, R> case2) {
      return value.fold(a->case1.test(a),()->case2.test(Tuple.tuple())).get();
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
