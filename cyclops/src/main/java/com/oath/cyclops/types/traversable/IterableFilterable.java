package com.oath.cyclops.types.traversable;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.oath.cyclops.types.Filters;
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
     * Remove all elements in the supplied Stream from this filterable
     *
     * @param stream of elements to remove
     * @return Filters with all supplied elements removed
     */
    default Filters<T> removeStream(final Stream<? extends T> stream) {
        final Eval<Set<T>> set = Eval.later(()->stream.collect(Collectors.toSet()));
        return filterNot(i -> set.get().contains(i));
    }

    /**
     * Remove all elements in the supplied Iterable from this filterable
     *
     * @param it  an Iterable of elements to removeValue
     * @return Filters with all supplied elements removed
     */
    default Filters<T> removeAll(final Iterable<? extends T> it) {
        return removeStream(Streams.stream(it));
    }

    /**
     * Remove all supplied elements from this filterable
     *
     * @param values to removeValue
     * @return Filters with all supplied values removed
     */
    default Filters<T> removeAll(final T... values) {
        return removeStream(Stream.of(values));

    }

    /**
     * Retain only the supplied elements in the returned Filters
     *
     * @param it Iterable of elements to retain
     * @return Filters with supplied values retained, and others removed
     */
    default Filters<T> retainAll(final Iterable<? extends T> it) {
        return retainStream(Streams.stream(it));
    }

    /**
     * Retain only the supplied elements in the returned Filters
     *
     * @param stream of elements to retain
     * @return Filters with supplied values retained, and others removed
     */
    default Filters<T> retainStream(final Stream<? extends T> stream) {
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
        return retainStream(Stream.of(values));
    }

}
