package com.aol.cyclops.types.mixins;

import java.util.function.Predicate;

import com.aol.cyclops.internal.monads.ComprehenderSelector;
import com.aol.cyclops.types.Filterable;

public interface WrappingFilterable<T> extends Filterable<T> {
    default Filterable<T> filter(Predicate<? super T> fn) {
        Object filterable = getFilterable();
        if (filterable instanceof Filterable) {
            return withFilterable((T) ((Filterable) filterable).filter(fn));
        }
        T result = (T) new ComprehenderSelector().selectComprehender(getFilterable())
                                                 .filter(filterable, fn);
        return withFilterable(result);
    }

    //ofType
    public Filterable<T> withFilterable(T filter);

    public Object getFilterable();
}
