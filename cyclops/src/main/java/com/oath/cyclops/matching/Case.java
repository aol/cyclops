package com.oath.cyclops.matching;

import static cyclops.control.Option.none;

import cyclops.control.Option;
import cyclops.data.tuple.Tuple1;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import cyclops.data.tuple.Tuple5;
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
    Option<R> test(T value);

    /**
     * Return the test for this user case or for the other. there is no guarantee
     * one successful result will be produced.
     *
     * @param orCase the case bo be tested in case this one fail.
     * @return a composite case to perform a XOR operation.
     */
    default Case<T, R> or(Case<T, R> orCase) {
        return (t) -> {
            Option<R> val = test(t);
            return val.isPresent() ? val : orCase.test(t);
        };
    }

    @AllArgsConstructor
    final class CaseFn<T, R> implements Case<T, R> {


        final Function<? super T, ? extends R> fn;

        @Override
        public Option<R> test(T value) {
            return Option.some(fn.apply(value));
        }

    }

    @AllArgsConstructor
    final class Case0<T, R> implements Case<T, R> {

        final Predicate<T> predicate;
        final Function<T, R> supplier;

        @Override
        public Option<R> test(T value) {
            return predicate.test(value) ? Option.some(supplier.apply(value)) : none();
        }

    }

    @AllArgsConstructor
    final class Case1<T1, R> implements Case<Tuple1<T1>, R> {

        final Predicate<T1> predicate;
        final Function<Tuple1<T1>, R> supplier;

        @Override
        public Option<R> test(Tuple1<T1> value) {
            return predicate.test(value._1()) ? Option.of(supplier.apply(value)) : none();
        }

    }

    @AllArgsConstructor
    final class Case2<T1, T2, R> implements Case<Tuple2<T1, T2>, R> {

        final Predicate<T1> predicate1;
        final Predicate<T2> predicate2;
        final Function<? super Tuple2<T1, T2>, ? extends R> supplier;

        @Override
        public Option<R> test(Tuple2<T1, T2> value) {
            return predicate1.test(value._1()) && predicate2.test(value._2()) ? Option.some(supplier.apply(value)) : none();
        }

    }

    @AllArgsConstructor
    final class Case3<T1, T2, T3, R> implements Case<Tuple3<T1, T2, T3>, R> {

        final Predicate<T1> predicate1;
        final Predicate<T2> predicate2;
        final Predicate<T3> predicate3;

        final Function<Tuple3<T1, T2, T3>, R> supplier;

        @Override
        public Option<R> test(Tuple3<T1, T2, T3> value) {
            return predicate1.test(value._1()) && predicate2.test(value._2()) && predicate3.test(value._3()) ? Option.some(supplier.apply(value)) : none();
        }

    }

    @AllArgsConstructor
    final class Case4<T1, T2, T3, T4, R> implements Case<Tuple4<T1, T2, T3, T4>, R> {

        final Predicate<T1> predicate1;
        final Predicate<T2> predicate2;
        final Predicate<T3> predicate3;
        final Predicate<T4> predicate4;

        final Function<Tuple4<T1, T2, T3, T4>, R> supplier;

        @Override
        public Option<R> test(Tuple4<T1, T2, T3, T4> value) {
            return predicate1.test(value._1()) && predicate2.test(value._2()) && predicate3.test(value._3()) && predicate4.test(value._4()) ? Option.some(supplier.apply(value)) : none();
        }

    }

    @AllArgsConstructor
    final class Case5<T1, T2, T3, T4, T5, R> implements Case<Tuple5<T1, T2, T3, T4, T5>, R> {

        final Predicate<T1> predicate1;
        final Predicate<T2> predicate2;
        final Predicate<T3> predicate3;
        final Predicate<T4> predicate4;
        final Predicate<T5> predicate5;

        final Function<Tuple5<T1, T2, T3, T4, T5>, R> supplier;

        @Override
        public Option<R> test(Tuple5<T1, T2, T3, T4, T5> value) {
            return predicate1.test(value._1()) && predicate2.test(value._2()) && predicate3.test(value._3()) && predicate4.test(value._4()) && predicate5.test(value._5()) ? Option.some(supplier.apply(value)) : none();
        }

    }

    @AllArgsConstructor
    final class CaseOptional<T, R> implements Case<Optional<T>, R> {
        @NonNull
        final Supplier<R> supplier0;
        @NonNull
        final Supplier<R> supplier1;

        @Override
        public Option<R> test(Optional<T> optional) {
            return Option.some(optional.isPresent() ? supplier0.get() : supplier1.get());
        }

    }

    /**
     * Marker interface to build a Default Case when no pattern matches.
     *
     * @param <R> return type.
     * @see cyclops.matching.Api#Any(Function)
     * @see cyclops.matching.Api#Any(Supplier)
     **/
    interface Any<T, R> extends Function<T, R> {

    }

}
