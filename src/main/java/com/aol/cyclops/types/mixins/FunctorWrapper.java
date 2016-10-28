package com.aol.cyclops.types.mixins;

import lombok.Value;
import lombok.experimental.Wither;

/**
 * Wrapper around a Functor
 * @author johnmcclean
 *
 * @param <T> Data type of elements transformed by wrapped functor
 */
@Value
public class FunctorWrapper<T> implements WrappingFunctor<T> {
    @Wither
    Object functor;

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.mixins.WrappingFunctor#getFunctor()
     */
    @Override
    public Object getFunctor() {
        return functor;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.mixins.WrappingFunctor#withFunctor(java.lang.Object)
     */
    @Override
    public <T> WrappingFunctor<T> withFunctor(final T functor) {
        return new FunctorWrapper(
                                  functor);
    }
}
