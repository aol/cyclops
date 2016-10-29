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
public interface Applicative<T> extends Functor<T> {
    
    
    
        
    
    /**
     * Lazily combine this Applicative with the supplied value via the supplied BiFunction
     * If this Applicative is a scalar value the provided value is combined with that value,
     * otherwise the value is combined pair wise with all value in a non-scalar datastructure
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
    default <T2, R> Applicative<R> combine(final Value<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {

        return (Applicative<R>) map(v -> Tuple.tuple(v, Curry.curry2(fn)
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
     *   Monoid<Maybe<Integer>> sumMaybes = Semigroups.combineApplicatives(Semigroups.intSum);
     * }
     * </pre>
     * 
     * @param combiner
     * @param app
     * @return
     */
    default  Applicative<T> combine(BinaryOperator<Applicative<T>> combiner,final Applicative<T> app) {
        return combiner.apply(this, app);
    }
}
