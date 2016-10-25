package com.aol.cyclops.types.mixins;

import lombok.Value;
import lombok.experimental.Wither;

@Value
public class FunctorWrapper<T> implements WrappingFunctor<T> {
    @Wither
    Object functor;

    @Override
    public Object getFunctor() {
        return functor;
    }

    @Override
    public <T> WrappingFunctor<T> withFunctor(final T functor) {
        return new FunctorWrapper(
                                  functor);
    }
}
