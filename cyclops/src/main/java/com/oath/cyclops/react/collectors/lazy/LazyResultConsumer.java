package com.oath.cyclops.react.collectors.lazy;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

import com.oath.cyclops.internal.react.async.future.FastFuture;

/**
 * Interface that defines the rules for Collecting results from Infinite SimpleReact Streams
 *
 * @author johnmcclean
 *
 */
public interface LazyResultConsumer<T> extends Consumer<FastFuture<T>> {

    /**
     * Used to generate a new instance for result toX - populates the supplied Collection
     *
     * @param t Collection to be populated
     * @return Consumer that will populate the toX
     */
    public LazyResultConsumer<T> withResults(Collection<FastFuture<T>> t);

    /**
     * @return Completed results
     */
    public Collection<FastFuture<T>> getResults();

    /**
     * @return Completed  and active results
     */
    public Collection<FastFuture<T>> getAllResults();

    public void block(Function<FastFuture<T>, T> safeJoin);

}
