package com.aol.cyclops.util;

import static com.aol.cyclops.control.AnyM.fromCompletableFuture;
import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.reactivestreams.Publisher;

import com.aol.cyclops.Reducer;
import com.aol.cyclops.Semigroup;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.Value;

public class CompletableFutures {

    public static <T> CompletableFuture<ListX<T>> sequence(final CollectionX<CompletableFuture<T>> fts) {
        return sequence(fts.stream()).thenApply(s -> s.toListX());
    }

    public static <T> CompletableFuture<ReactiveSeq<T>> sequence(final Stream<CompletableFuture<T>> fts) {
        return AnyM.sequence(fts.map(f -> fromCompletableFuture(f)), () -> AnyM.fromCompletableFuture(completedFuture(Stream.<T> empty())))
                   .map(s -> ReactiveSeq.fromStream(s))
                   .unwrap();

    }

    public static <T, R> CompletableFuture<R> accumulateSuccess(final CollectionX<CompletableFuture<T>> fts, final Reducer<R> reducer) {

        final CompletableFuture<ListX<T>> sequenced = AnyM.sequence(fts.map(f -> AnyM.fromCompletableFuture(f)))
                                                          .unwrap();
        return sequenced.thenApply(s -> s.mapReduce(reducer));
    }

    public static <T, R> CompletableFuture<R> accumulate(final CollectionX<CompletableFuture<T>> fts, final Reducer<R> reducer) {
        return sequence(fts).thenApply(s -> s.mapReduce(reducer));
    }

    public static <T, R> CompletableFuture<R> accumulate(final CollectionX<CompletableFuture<T>> fts, final Function<? super T, R> mapper,
            final Semigroup<R> reducer) {
        return sequence(fts).thenApply(s -> s.map(mapper)
                                             .reduce(reducer)
                                             .get());
    }

    public static <T> CompletableFuture<T> schedule(final String cron, final ScheduledExecutorService ex, final Supplier<T> t) {
        return FutureW.schedule(cron, ex, t)
                      .getFuture();
    }

    public static <T> CompletableFuture<T> schedule(final long delay, final ScheduledExecutorService ex, final Supplier<T> t) {
        return FutureW.schedule(delay, ex, t)
                      .getFuture();
    }

    public static <T1, T2, R> CompletableFuture<R> combine(final CompletableFuture<? extends T1> f, final Value<? extends T2> v,
            final BiFunction<? super T1, ? super T2, ? extends R> fn) {
        return narrow(FutureW.of(f)
                             .combine(v, fn)
                             .getFuture());
    }

    public static <T1, T2, R> CompletableFuture<R> zip(final CompletableFuture<? extends T1> f, final Iterable<? extends T2> v,
            final BiFunction<? super T1, ? super T2, ? extends R> fn) {
        return narrow(FutureW.of(f)
                             .zip(v, fn)
                             .getFuture());
    }

    public static <T1, T2, R> CompletableFuture<R> zip(final Publisher<? extends T2> p, final CompletableFuture<? extends T1> f,
            final BiFunction<? super T1, ? super T2, ? extends R> fn) {
        return narrow(FutureW.of(f)
                             .zip(fn, p)
                             .getFuture());
    }

    public static <T> CompletableFuture<T> narrow(final CompletableFuture<? extends T> f) {
        return (CompletableFuture<T>) f;
    }
}
