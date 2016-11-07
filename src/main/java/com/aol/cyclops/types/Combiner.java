package com.aol.cyclops.types;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

import org.jooq.lambda.tuple.Tuple;

import com.aol.cyclops.util.function.Curry;

/**
 * Combinable type via BiFunctions / Monoids / Semigroups
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of the element/s inside this Applicative
 */
public interface Combiner<T> extends Functor<T>, Value<T>{
    /**
     * Lazily combine this ApplicativeFunctor with the supplied value via the supplied BiFunction
     * 
     * Example
     * <pre>
     * {@code 
     *   Maybe<Integer> some = Maybe.just(10);
     *   just.combine(Eval.now(20), this::add);
     *   //Some[30]
     *   
     *   Maybe<Integer> none = Maybe.none();
     *   none.combine(Eval.now(20), this::add);
     *   //None
     *   
     * }
     * </pre>
     * 
     * @param app Value to combine with this one.
     * @param fn BiFunction to combine them
     * @return New Applicativefunctor that represents the combined values
     */
    default <T2, R> Functor<R> combine(final Value<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {

        return (Functor<R>) map(v -> Tuple.tuple(v, Curry.curry2(fn)
                                                                    .apply(v))).map(tuple -> app.visit(i -> tuple.v2.apply(i), () -> tuple.v1));
    }

   
    /**
     * Combine two applicatives together using the provided BinaryOperator (Semigroup, Monoid and Reducer all
     * extend BinaryOperator - checkout Semigroups and Monoids for a large number of canned combinations).
     * If this Applicative is a scalar value the provided value is combined with that value,
     * otherwise the value is combined pair wise with all value in a non-scalar datastructure
     * 
     * @see com.aol.cyclops.Semigroup
     * @see com.aol.cyclops.Semigroups
     * @see com.aol.cyclops.Monoid
     * @see com.aol.cyclops.Monoids
     * 
     * To lift any Semigroup (or monoid) up to handling Applicatives use the combineApplicatives operator in Semigroups
     * {@see com.aol.cyclops.Semigroups#combineApplicatives(BiFunction) } or Monoids 
     * { {@see com.aol.cyclops.Monoids#combineApplicatives(java.util.function.Function, com.aol.cyclops.Monoid)
     *  }
     * <pre>
     * {@code 
     * 
     *   
     *  BinaryOperator<Combiner<Integer>> sumMaybes = Semigroups.combineScalarFunctors(Semigroups.intSum);
        Maybe.just(1)
            .combine(sumMaybes, Maybe.just(5))
        //Maybe.just(6));
     * }
     * </pre>
     * 
     * @param combiner
     * @param app
     * @return
     */
    default  Combiner<T> combine(BinaryOperator<Combiner<T>> combiner,final Combiner<T> app) {
        return combiner.apply(this, app);
    }
}
