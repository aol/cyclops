package com.aol.cyclops2.react.collectors.lazy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.aol.cyclops2.internal.react.async.future.FastFuture;
import com.aol.cyclops2.types.futurestream.BlockingStream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Builder;
import lombok.experimental.Wither;

/**
 * This class allows a Batch of completable futures toNested be processed before collecting their results, toNested increase
 * parallelism.
 * 
 * @author johnmcclean
 *
 * @param <T> Result type
 */
@Wither
@AllArgsConstructor
@Builder
public class BatchingCollector<T> implements LazyResultConsumer<T> {

    @Getter
    private final Collection<FastFuture<T>> results;
    private final List<FastFuture<T>> active = new ArrayList<>();
    @Getter
    private final MaxActive maxActive;
    @Getter
    private final BlockingStream<T> blocking;

    /**
     * @param maxActive Controls batch size
     */
    public BatchingCollector(final MaxActive maxActive, final BlockingStream<T> blocking) {
        this.maxActive = maxActive;
        this.results = null;
        this.blocking = blocking;
    }

    /* (non-Javadoc)
     * @see java.util.function.Consumer#accept(java.lang.Object)
     */
    @Override
    public void accept(final FastFuture<T> t) {

        active.add(t);

        if (active.size() > maxActive.getMaxActive()) {

            while (active.size() > maxActive.getReduceTo()) {

                final List<FastFuture<T>> toRemove = active.stream()
                                                           .filter(cf -> cf.isDone())
                                                           .collect(Collectors.toList());
                active.removeAll(toRemove);
                results.addAll(toRemove);
                if (active.size() > maxActive.getReduceTo()) {
                    final CompletableFuture promise = new CompletableFuture();
                    FastFuture.xOf(active.size() - maxActive.getReduceTo(), () -> {
                        promise.complete(true);
                    } , active.toArray(new FastFuture[0]));

                    promise.join();
                }

            }
        }

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.react.collectors.lazy.LazyResultConsumer#block(java.util.function.Function)
     */
    @Override
    public void block(final Function<FastFuture<T>, T> safeJoin) {
        if (active.size() == 0)
            return;
        active.stream()
              .peek(f -> safeJoin.apply(f))
              .forEach(a -> {
              });

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.react.collectors.lazy.LazyResultConsumer#getResults()
     */
    @Override
    public Collection<FastFuture<T>> getResults() {

        return results;
    }

    /* 
     *	@return all results (including active)
     * @see com.aol.cyclops2.react.collectors.lazy.LazyResultConsumer#getAllResults()
     */
    @Override
    public Collection<FastFuture<T>> getAllResults() {
        results.addAll(active);
        active.clear();
        return results;
    }

}