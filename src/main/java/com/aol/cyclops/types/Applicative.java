package com.aol.cyclops.types;

import java.util.function.BinaryOperator;

/**
 * Combinable type via BiFunctions / Monoids / Semigroups
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of the element/s inside this Applicative
 */
public interface Applicative<T>{
    

   
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
