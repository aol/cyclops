package com.aol.cyclops.types.mixins;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;
import java.util.function.Function;

import com.aol.cyclops.internal.monads.ComprehenderSelector;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.util.ExceptionSoftener;

/**
 * An interoperability trait that wraps &amp; encapsulates any Functor type
 * 
 * Uses InvokeDynamic to call Map if no suitable Comprehender present
 * Uses (cached) JDK Dynamic Proxies to coerce function types to java.util.Function
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of elements transformed by wrapped functor
 */

public interface WrappingFunctor<T> extends Functor<T> {

    /**
     * Will attempt to create a new instance of this functor type via constructor reflection
     * if this is a wrapped Functor (i.e. getFunctor returns another instance) otherwise
     * returns the supplied functor
     * 
     * 
     * @param functor
     * @return
     */
    default <T> WrappingFunctor<T> withFunctor(final T functor) {
        if (getFunctor() == this)
            return (WrappingFunctor<T>) functor;

        try {
            final Constructor cons = getClass().getConstructor(Object.class);
            cons.setAccessible(true);
            return (WrappingFunctor) cons.newInstance(functor);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException e) {
            ExceptionSoftener.throwSoftenedException(e);
            return null;
        }

    }

    /**
     * Override this method if you are using this class to wrap a Functor that does not
     * implement this interface natively.
     * 
     * @return underlying functor
     */
    default Object getFunctor() {
        return this;
    }

    @Override
    default <R> WrappingFunctor<R> map(final Function<? super T, ? extends R> fn) {
        final Object functor = getFunctor();
        if (functor instanceof Functor) {
            final Functor f = (Functor) functor;

            return withFunctor((R) f.map(fn));
        }
        final Object value = new ComprehenderSelector().selectComprehender(getFunctor())
                                                       .map(getFunctor(), fn);

        return withFunctor((R) value);

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#peek(java.util.function.Consumer)
     */
    @Override
    default WrappingFunctor<T> peek(final Consumer<? super T> c) {
        return map(input -> {
            c.accept(input);
            return input;
        });
    }

    /**
     * @return Unwrapped functor
     */
    default <X> X unwrap() {
        if (getFunctor() != this && getFunctor() instanceof Functor)
            return (X) ((WrappingFunctor) getFunctor()).unwrap();
        return (X) getFunctor();
    }

}
