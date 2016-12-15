package com.aol.cyclops.types.anyM.transformers;

import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.types.ConvertableFunctor;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.applicative.ApplicativeFunctor;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface TransformerValue<T>
        extends MonadicValue<T>, Supplier<T>, ConvertableFunctor<T>, Filterable<T>, ApplicativeFunctor<T> {

    public boolean isValuePresent();

    public MonadicValue<T> value();

    @Override
    default boolean isPresent() {
        return isValuePresent();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filter(java.util.function.Predicate)
     */
    @Override
    TransformerValue<T> filter(Predicate<? super T> fn);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#unit(java.lang.Object)
     */
    @Override
    <T> TransformerValue<T> unit(T unit);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#map(java.util.function.Function)
     */
    @Override
    <R> TransformerValue<R> map(Function<? super T, ? extends R> fn);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#cast(java.lang.Class)
     */
    @Override
    default <U> TransformerValue<U> cast(final Class<? extends U> type) {

        return (TransformerValue<U>) MonadicValue.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#peek(java.util.function.Consumer)
     */
    @Override
    default TransformerValue<T> peek(final Consumer<? super T> c) {

        return (TransformerValue<T>) MonadicValue.super.peek(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#trampoline(java.util.function.Function)
     */
    @Override
    default <R> TransformerValue<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {

        return (TransformerValue<R>) MonadicValue.super.trampoline(mapper);
    }



}
