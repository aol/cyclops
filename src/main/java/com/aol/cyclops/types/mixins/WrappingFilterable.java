package com.aol.cyclops.types.mixins;

import java.util.function.Predicate;

import com.aol.cyclops.internal.monads.ComprehenderSelector;
import com.aol.cyclops.types.Filterable;
//@TODO move to internal 2.0.0
@Deprecated //internal interface - move in 2.0.0
public interface WrappingFilterable<T> extends Filterable<T> {
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filter(java.util.function.Predicate)
     */
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

    
    /**
     * @param filterable Wrapped filterable
     * @return Filterable wrapping new Filterable
     */
    public Filterable<T> withFilterable(T filterable);

    /**
     * @return Wrapped filterable
     */
    public Object getFilterable();
}
