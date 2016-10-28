package com.aol.cyclops.types.mixins;

/**
 * Wrapper for an Object that can behave as a Functor
 * 
 * @author johnmcclean
 *
 */
public class AsFunctor {

    /**
     * Create a Duck typed functor. Wrapped class should have a method
     * 
     * map(F f)
     * 
     * <pre>
     * {@code 
     * 
     * Functor<Integer> mappedStream = AsFunctor.asFunctor(Stream.of(1,2,3))
     *                                          .map( i->i*2);
       
     * 
     * }
     * </pre>
     * 
     * 
     * Where F is a Functional Interface of any type that takes a single parameter and returns
     * a result.
     * 
     * @param o functor to wrap
     * @return Duck typed functor
     */
    public static <T> WrappingFunctor<T> asFunctor(final Object o) {
        return new FunctorWrapper<>(
                                    o);
    }

}
