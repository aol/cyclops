package com.aol.cyclops.types.mixins;

import java.util.function.Predicate;

import com.aol.cyclops.internal.monads.ComprehenderSelector;
import com.aol.cyclops.types.Filterable;

public interface WrappingFilterable<T> extends Filterable<T> {
    @Override
    default Filterable<T> filter(final Predicate<? super T> fn) {
        final Object filterable = getFilterable();
        if (filterable instanceof Filterable) {
            return withFilterable((T) ((Filterable) filterable).filter(fn));
        }
        final T result = (T) new ComprehenderSelector().selectComprehender(getFilterable())
                                                       .filter(filterable, fn);
        return withFilterable(result);
    }

    //ofType
    public Filterable<T> withFilterable(T filter);

    public Object getFilterable();
}
