package com.aol.cyclops2.react.collectors.lazy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.aol.cyclops2.internal.react.async.future.FastFuture;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

/**
 * A collector that periodically joins active completablefutures
 * but does not store the results
 * 
 * @author johnmcclean
 *
 */
@Wither
@AllArgsConstructor
public class EmptyCollector<T> implements LazyResultConsumer<T> {

    private final List<FastFuture<T>> active = new ArrayList<>();
    @Getter
    private final MaxActive maxActive;
    @Getter
    private final Function<FastFuture<T>, T> safeJoin;

    EmptyCollector() {
        maxActive = MaxActive.IO;
        safeJoin = cf -> cf.join();
    }

    /* 
     *	@param t Result type
     * @see java.util.function.Consumer#accept(java.lang.Object)
     */
    @Override
    public void accept(final FastFuture<T> t) {

        active.add(t);

        if (active.size() > maxActive.getMaxActive()) {

            while (active.size() > maxActive.getReduceTo()) {

                final List<FastFuture> toRemove = active.stream()
                                                        .filter(cf -> cf.isDone())
                                                        .peek(this::handleExceptions)
                                                        .collect(Collectors.toList());

                active.removeAll(toRemove);
                if (active.size() > maxActive.getReduceTo()) {
                    final CompletableFuture promise = new CompletableFuture();
                    FastFuture.xOf(active.size() - maxActive.getReduceTo(), () -> promise.complete(true), active.toArray(new FastFuture[0]));

                    promise.join();
                }

            }
        }

    }

    public void add(final FastFuture<T> t) {
        active.add(t);
    }

    private void handleExceptions(final FastFuture cf) {
        if (cf.isCompletedExceptionally())
            safeJoin.apply(cf);
    }

    @Override
    public EmptyCollector<T> withResults(final Collection<FastFuture<T>> t) {

        return this.withMaxActive(maxActive);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.react.collectors.lazy.LazyResultConsumer#block(java.util.function.Function)
     */
    @Override
    public void block(final Function<FastFuture<T>, T> safeJoin) {

        if (active.size() == 0)
            return;
        active.stream()
              .peek(cf -> safeJoin.apply(cf))
              .forEach(a -> {
              });

    }

    /* 
     *	@return empty list
     * @see com.aol.cyclops2.react.collectors.lazy.LazyResultConsumer#getResults()
     */
    @Override
    public Collection<FastFuture<T>> getResults() {
        active.stream()
              .forEach(cf -> safeJoin.apply(cf));
        active.clear();
        return new ArrayList<>();
    }

    /* 
     *	@return empty list
     * @see com.aol.cyclops2.react.collectors.lazy.LazyResultConsumer#getAllResults()
     */
    @Override
    public Collection<FastFuture<T>> getAllResults() {
        return getResults();
    }

    public boolean hasCapacity(final int i) {
        return maxActive.getMaxActive() + i > active.size();
    }

}