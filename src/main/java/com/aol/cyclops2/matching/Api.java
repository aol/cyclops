package com.aol.cyclops2.matching;

import com.aol.cyclops2.matching.Case.Any;
import com.aol.cyclops2.matching.Case.Case2;
import com.aol.cyclops2.matching.Case.Case3;
import com.aol.cyclops2.matching.Case.Case4;
import com.aol.cyclops2.matching.Case.Case5;
import com.aol.cyclops2.matching.Case.CaseOptional;
import com.aol.cyclops2.matching.Matching.OptionalMatching;
import com.aol.cyclops2.matching.Matching.SumMatching;
import com.aol.cyclops2.matching.types.Deconstruct;
import com.aol.cyclops2.matching.types.Deconstruct.Deconstruct1;
import com.aol.cyclops2.matching.types.Deconstruct.Deconstruct2;
import com.aol.cyclops2.matching.types.Deconstruct.Deconstruct3;
import com.aol.cyclops2.matching.types.Deconstruct.Deconstruct4;
import com.aol.cyclops2.matching.types.Deconstruct.Deconstruct5;

import org.jooq.lambda.tuple.Tuple1;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.jooq.lambda.tuple.Tuple5;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Api {

  private Api() {
  }

  public static <T> OptionalMatching<T> Match(final Optional<T> value) {
    return new OptionalMatching<>(value);
  }

  public static <T> SumMatching<T> Match(T value) {
    return new SumMatching<>(value);
  }

  public static <T extends Deconstruct<T1>, T1> SumMatching<T1> Match(T value) {
    return new SumMatching<>(value.deconstruct());
  }

  public static <T extends Deconstruct1<T1>, T1> SumMatching<Tuple1<T1>> Match(T value) {
    return new SumMatching<>(value.deconstruct());
  }

  public static <T extends Deconstruct2<T1, T2>, T1, T2> SumMatching<Tuple2<T1, T2>> Match(T value) {
    return new SumMatching<>(value.deconstruct());
  }

  public static <T extends Deconstruct3<T1, T2, T3>, T1, T2, T3> SumMatching<Tuple3<T1, T2, T3>> Match(T value) {
    return new SumMatching<>(value.deconstruct());
  }

  public static <T extends Deconstruct4<T1, T2, T3, T4>, T1, T2, T3, T4> SumMatching<Tuple4<T1, T2, T3, T4>> Match(T value) {
    return new SumMatching<>(value.deconstruct());
  }

  public static <T extends Deconstruct5<T1, T2, T3, T4, T5>, T1, T2, T3, T4, T5> SumMatching<Tuple5<T1, T2, T3, T4, T5>> Match(T value) {
    return new SumMatching<>(value.deconstruct());
  }

  public static <T, R> Case<Optional<T>, R> Case(Supplier<R> supplier0, Supplier<R> supplier1) {
    return new CaseOptional<>(supplier0, supplier1);
  }

  public static <T, R> Case<T, R> Case(Predicate<T> predicate, Supplier<R> supplier) {
    return new Case.Case0<>(predicate, supplier);
  }

  public static <T1, T2, R> Case<Tuple2<T1, T2>, R> Case(Predicate<T1> predicate1, Predicate<T2> predicate2, Supplier<R> supplier) {
    return new Case2<>(predicate1, predicate2, supplier);
  }

  public static <T1, T2, T3, R> Case<Tuple3<T1, T2, T3>, R> Case(Predicate<T1> predicate1, Predicate<T2> predicate2, Predicate<T3> predicate3, Supplier<R> supplier) {
    return new Case3<>(predicate1, predicate2, predicate3, supplier);
  }

  public static <T1, T2, T3, T4, R> Case<Tuple4<T1, T2, T3, T4>, R> Case(Predicate<T1> predicate1, Predicate<T2> predicate2, Predicate<T3> predicate3, Predicate<T4> predicate4, Supplier<R> supplier) {
    return new Case4<>(predicate1, predicate2, predicate3, predicate4, supplier);
  }

  public static <T1, T2, T3, T4, T5, R> Case<Tuple5<T1, T2, T3, T4, T5>, R> Case(Predicate<T1> predicate1, Predicate<T2> predicate2, Predicate<T3> predicate3, Predicate<T4> predicate4, Predicate<T5> predicate5, Supplier<R> supplier) {
    return new Case5<>(predicate1, predicate2, predicate3, predicate4, predicate5, supplier);
  }

  public static <R> Any<R> Any(Supplier<R> supplier) {
    return supplier::get;
  }

}
