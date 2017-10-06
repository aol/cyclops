package com.aol.cyclops2.matching;

import static java.util.Optional.empty;

import org.jooq.lambda.tuple.Tuple1;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.jooq.lambda.tuple.Tuple5;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Pattern matching use case contract.
 *
 * @param <T> the type that is being tested.
 * @param <R> the return that the use case will return, it may or note return null.
 */
@FunctionalInterface
public interface Case<T, R> {

  /**
   * Test the use case and return in case the test is true.
   *
   * @param value the value to be tested.
   * @return the return, implementations may or not return null to determine success.
   */
  Optional<R> test(T value);

  /**
   * Return the test for this user case or for the other. there is no guarantee
   * one successful result will be produced.
   *
   * @param orCase the case bo be tested in case this one fail.
   * @return a composite case to perform a XOR operation.
   */
  default Case<T, R> or(Case<T, R> orCase) {
    return (t) -> {
      Optional<R> val = test(t);
      return val.isPresent() ? val : orCase.test(t);
    };
  }
  @AllArgsConstructor
  final class CaseFn<T, R> implements Case<T, R>{


    final Function<? super T,? extends R> fn;

    @Override
    public Optional<R> test(T value) {
      return Optional.of(fn.apply(value));
    }

  }
  @AllArgsConstructor
  final class Case0<T, R> implements Case<T, R> {

    final Predicate<T> predicate;
    final Supplier<R> supplier;

    @Override
    public Optional<R> test(T value) {
      return predicate.test(value) ? Optional.of(supplier.get()) : empty();
    }

  }

  @AllArgsConstructor
  final class Case1<T1, R> implements Case<Tuple1<T1>, R> {

    final Predicate<T1> predicate;
    final Supplier<R> supplier;

    @Override
    public Optional<R> test(Tuple1<T1> value) {
      return predicate.test(value.v1) ? Optional.of(supplier.get()) : empty();
    }

  }

  @AllArgsConstructor
  final class Case2<T1, T2, R> implements Case<Tuple2<T1, T2>, R> {

    final Predicate<T1> predicate1;
    final Predicate<T2> predicate2;
    final Function<? super Tuple2<T1,T2>,? extends R> supplier;

    @Override
    public Optional<R> test(Tuple2<T1, T2> value) {
      return predicate1.test(value.v1) && predicate2.test(value.v2) ? Optional.of(supplier.apply(value)) : empty();
    }

  }

  @AllArgsConstructor
  final class Case3<T1, T2, T3, R> implements Case<Tuple3<T1, T2, T3>, R> {

    final Predicate<T1> predicate1;
    final Predicate<T2> predicate2;
    final Predicate<T3> predicate3;

    final Supplier<R> supplier;

    @Override
    public Optional<R> test(Tuple3<T1, T2, T3> value) {
      return predicate1.test(value.v1) && predicate2.test(value.v2) && predicate3.test(value.v3) ? Optional.of(supplier.get()) : empty();
    }

  }

  @AllArgsConstructor
  final class Case4<T1, T2, T3, T4, R> implements Case<Tuple4<T1, T2, T3, T4>, R> {

    final Predicate<T1> predicate1;
    final Predicate<T2> predicate2;
    final Predicate<T3> predicate3;
    final Predicate<T4> predicate4;

    final Supplier<R> supplier;

    @Override
    public Optional<R> test(Tuple4<T1, T2, T3, T4> value) {
      return predicate1.test(value.v1) && predicate2.test(value.v2) && predicate3.test(value.v3) && predicate4.test(value.v4) ? Optional.of(supplier.get()) : empty();
    }

  }

  @AllArgsConstructor
  final class Case5<T1, T2, T3, T4, T5, R> implements Case<Tuple5<T1, T2, T3, T4, T5>, R> {

    final Predicate<T1> predicate1;
    final Predicate<T2> predicate2;
    final Predicate<T3> predicate3;
    final Predicate<T4> predicate4;
    final Predicate<T5> predicate5;

    final Supplier<R> supplier;

    @Override
    public Optional<R> test(Tuple5<T1, T2, T3, T4, T5> value) {
      return predicate1.test(value.v1) && predicate2.test(value.v2) && predicate3.test(value.v3) && predicate4.test(value.v4) && predicate5.test(value.v5) ? Optional.of(supplier.get()) : empty();
    }

  }

  @AllArgsConstructor
  final class CaseOptional<T, R> implements Case<Optional<T>, R> {
    @NonNull
    final Supplier<R> supplier0;
    @NonNull
    final Supplier<R> supplier1;

    @Override
    public Optional<R> test(Optional<T> optional) {
      return Optional.of(optional.isPresent() ? supplier0.get() : supplier1.get());
    }

  }

  /**
   * Marker interface to build a Default Case when no pattern matches.
   *
   * @param <R> return type.
   * @see Api#Any(Supplier)
   **/
  interface Any<R> extends Supplier<R> {

  }

}
