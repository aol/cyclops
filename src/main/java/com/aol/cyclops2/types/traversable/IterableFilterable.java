package com.aol.cyclops2.types.traversable;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aol.cyclops2.types.Filters;
import cyclops.companion.Streams;
import cyclops.control.Eval;

/**
 * An interface that represents a non-scalar Filters
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of elements stored in this IterableFilterable
 */
public interface IterableFilterable<T> extends Filters<T>, Iterable<T> {

    /**
     * Remove all elements in the supplied LazyList from this filterable
     * 
     * @param stream of elements to remove
     * @return Filters with all supplied elements removed
     */
    default Filters<T> removeAllS(final Stream<? extends T> stream) {
        final Eval<Set<T>> set = Eval.later(()->stream.collect(Collectors.toSet()));
        return filterNot(i -> set.get().contains(i));
    }

    /**
     * Remove all elements in the supplied Iterable from this filterable 
     * 
     * @param it  an Iterable of elements to remove
     * @return Filters with all supplied elements removed
     */
    default Filters<T> removeAllI(final Iterable<? extends T> it) {
        return removeAllS(Streams.stream(it));
    }

    /**
     * Remove all supplied elements from this filterable   
     * 
     * @param values to remove
     * @return Filters with all supplied values removed
     */
    default Filters<T> removeAll(final T... values) {
        return removeAllS(Stream.of(values));

    }

    /**
     * Retain only the supplied elements in the returned Filters
     * 
     * @param it Iterable of elements to retain
     * @return Filters with supplied values retained, and others removed
     */
    default Filters<T> retainAllI(final Iterable<? extends T> it) {
        return retainAllS(Streams.stream(it));
    }

    /**
     * Retain only the supplied elements in the returned Filters
     * 
     * @param stream of elements to retain
     * @return Filters with supplied values retained, and others removed
     */
    default Filters<T> retainAllS(final Stream<? extends T> stream) {
        final Eval<Set<T>> set = Eval.later(()->stream.collect(Collectors.toSet()));
        return filter(i -> set.get().contains(i));
    }

    /**
     * Retain only the supplied elements in the returned Filters
     * 
     * @param values elements to retain
     * @return Filters with supplied values retained, and others removed
     */
    default Filters<T> retainAll(final T... values) {
        return retainAllS(Stream.of(values));
    }

}
