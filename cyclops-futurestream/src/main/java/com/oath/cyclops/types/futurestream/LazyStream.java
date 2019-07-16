package com.oath.cyclops.types.futurestream;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.oath.cyclops.internal.react.async.future.FastFuture;
import com.oath.cyclops.internal.react.exceptions.SimpleReactProcessingException;
import com.oath.cyclops.internal.react.stream.MissingValue;
import com.oath.cyclops.internal.react.stream.Runner;
import com.oath.cyclops.react.collectors.lazy.LazyResultConsumer;
import com.oath.cyclops.react.collectors.lazy.MaxActive;
import com.oath.cyclops.react.threads.SequentialElasticPools;
import cyclops.futurestream.SimpleReact;
import com.oath.cyclops.internal.react.stream.LazyStreamWrapper;
import com.oath.cyclops.react.collectors.lazy.EmptyCollector;
import com.oath.cyclops.react.collectors.lazy.IncrementalReducer;

public interface LazyStream<U> extends BlockingStream<U> {

    @Override
    LazyStreamWrapper<U> getLastActive();

    Supplier<LazyResultConsumer<U>> getLazyCollector();

    @Override
    Optional<Consumer<Throwable>> getErrorHandler();

    MaxActive getMaxActive();

    public Iterator<U> iterator();

    /**
     * Trigger a lazy stream as a task on the provided Executor
     *
     *
     *
     *
     */
    default void run() {
        final SimpleReact reactor = SequentialElasticPools.simpleReact.nextReactor();
        reactor.ofAsync(() -> run(new NonCollector<>()))
               .peek(n -> SequentialElasticPools.simpleReact.populate(reactor))
               .onFail(n -> {
                   SequentialElasticPools.simpleReact.populate(reactor);
                   return 1;
               });

    }

    default void runThread(final Runnable r) {
        final Function<FastFuture, U> safeJoin = (final FastFuture cf) -> (U) BlockingStreamHelper.getSafe(cf, getErrorHandler());
        new Thread(
                   () -> new Runner(
                                    r).run(getLastActive(),
                                           new EmptyCollector(
                                                              getMaxActive(), safeJoin))).start();

    }

    default Continuation runContinuation(final Runnable r) {
        final Function<FastFuture, U> safeJoin = (final FastFuture cf) -> (U) BlockingStreamHelper.getSafe(cf, getErrorHandler());
        return new Runner(
                          r).runContinuations(getLastActive(),
                                              new EmptyCollector(
                                                                 getMaxActive(), safeJoin),false);

    }
    default Continuation blockingContinuation(final Runnable r) {
        final Function<FastFuture, U> safeJoin = (final FastFuture cf) -> (U) BlockingStreamHelper.getSafe(cf, getErrorHandler());
        return new Runner(
            r).runContinuations(getLastActive(),
            new EmptyCollector(
                getMaxActive(), safeJoin),true);

    }

    /**
     * Trigger a lazy stream
     */
    default void runOnCurrent() {

        run(new NonCollector());

    }

    /**
     * Trigger a lazy stream and return the results in the Collection created by
     * the collector
     *
     * @param collector
     *            Supplier that creates a toX to store results in
     * @return Collection of results
     */
    default <A, R> R run(final Collector<U, A, R> collector) {
        if (getLastActive().isSequential()) {
            //if single threaded we can simply push from each Future into the toX to be returned
            if (collector.supplier()
                         .get() == null) {
                forEach(r -> {
                });
                return null;
            }
            final A col = collector.supplier()
                                   .get();
            forEach(r -> collector.accumulator()
                                  .accept(col, r));
            return collector.finisher()
                            .apply(col);

        }

        final Function<FastFuture<U>, U> safeJoin = (final FastFuture<U> cf) -> (U) BlockingStreamHelper.getSafe(cf, getErrorHandler());
        final LazyResultConsumer<U> batcher = collector.supplier()
                                                       .get() != null
                                                               ? getLazyCollector().get()
                                                                                   .withResults(new ArrayList<>())
                                                               : new EmptyCollector<>(
                                                                                      this.getMaxActive(), safeJoin);

        try {

            this.getLastActive()
                .injectFutures()
                .forEach(n -> {

                    batcher.accept(n);

                });
        } catch (final SimpleReactProcessingException e) {

        }
        if (collector.supplier()
                     .get() == null) {
            batcher.block(safeJoin);
            return null;
        }

        return (R) batcher.getAllResults()
                          .stream()
            .map(cf -> BlockingStreamHelper.extractNonFiltered(cf, getErrorHandler()))
                          //.map(cf -> BlockingStreamHelper.getSafe(cf, getErrorHandler()))
                          .filter(v -> v != MissingValue.MISSING_VALUE)
                          .collect((Collector) collector);

    }

    default void forEach(final Consumer<? super U> c) {
        final Function<FastFuture<U>, U> safeJoin = (final FastFuture<U> cf) -> (U) BlockingStreamHelper.getSafe(cf, getErrorHandler());

        if (getLastActive().isSequential()) {
            //if single threaded we can simply push from each Future into the toX to be returned
            try {

                this.getLastActive()
                    .operation(f -> f.peek(c))
                    .injectFutures()
                    .forEach(next -> {

                        safeJoin.apply(next);


                    });
            } catch (final SimpleReactProcessingException e) {

            }

            return;
        }

        final IncrementalReducer<U> collector = new IncrementalReducer(
                                                                       this.getLazyCollector()
                                                                           .get()
                                                                           .withResults(new ArrayList<>()),
                                                                       this);
        try {
            this.getLastActive()
                .operation(f -> f.peek(c))
                .injectFutures()
                .forEach(next -> {

                    collector.getConsumer()
                             .accept(next);

                });
        } catch (final SimpleReactProcessingException e) {

        }
        collector.getConsumer()
                 .block(safeJoin);

    }

    default Optional<U> reduce(final BinaryOperator<U> accumulator) {
        if (getLastActive().isSequential()) {
            final Object[] result = { null };
            forEach(r -> {
                if (result[0] == null)
                    result[0] = r;
                else {
                    result[0] = accumulator.apply((U) result[0], r);
                }
            });
            return (Optional) Optional.ofNullable(result[0]);

        }
        final Function<FastFuture, U> safeJoin = (final FastFuture cf) -> (U) BlockingStreamHelper.getSafe(cf, getErrorHandler());
        final IncrementalReducer<U> collector = new IncrementalReducer(
                                                                       this.getLazyCollector()
                                                                           .get()
                                                                           .withResults(new ArrayList<>()),
                                                                       this);
        final Optional[] result = { Optional.empty() };
        try {
            this.getLastActive()
                .injectFutures()
                .forEach(next -> {
                    collector.getConsumer()
                             .accept(next);
                    if (!result[0].isPresent())
                        result[0] = collector.reduce(safeJoin, accumulator);
                    else
                        result[0] = result[0].map(v -> collector.reduce(safeJoin, (U) v, accumulator));

                });
        } catch (final SimpleReactProcessingException e) {

        }

        if (result[0].isPresent())
            return result[0].map(v -> collector.reduceResults(collector.getConsumer()
                                                                       .getAllResults(),
                                                              safeJoin, (U) v, accumulator));

        return collector.reduceResults(collector.getConsumer()
                                                .getAllResults(),
                                       safeJoin, accumulator);

    }

    default U reduce(final U identity, final BinaryOperator<U> accumulator) {
        if (getLastActive().isSequential()) {
            final Object[] result = { identity };
            forEach(r -> {
                if (result[0] == null)
                    result[0] = r;
                else {
                    result[0] = accumulator.apply((U) result[0], r);
                }
            });
            return (U) result[0];

        }
        final Function<FastFuture, U> safeJoin = (final FastFuture cf) -> (U) BlockingStreamHelper.getSafe(cf, getErrorHandler());
        final IncrementalReducer<U> collector = new IncrementalReducer(
                                                                       this.getLazyCollector()
                                                                           .get()
                                                                           .withResults(new ArrayList<>()),
                                                                       this);
        final Object[] result = { identity };
        try {
            this.getLastActive()
                .injectFutures()
                .forEach(next -> {

                    collector.getConsumer()
                             .accept(next);

                    result[0] = collector.reduce(safeJoin, (U) result[0], accumulator);
                });
        } catch (final SimpleReactProcessingException e) {

        }
        return collector.reduceResults(collector.getConsumer()
                                                .getAllResults(),
                                       safeJoin, (U) result[0], accumulator);
    }

    default <T> T reduce(final T identity, final BiFunction<T, ? super U, T> accumulator, final BinaryOperator<T> combiner) {

        if (getLastActive().isSequential()) {
            final Object[] result = { identity };
            forEach(r -> {
                if (result[0] == null)
                    result[0] = r;
                else {
                    result[0] = accumulator.apply((T) result[0], r);
                }
            });
            return (T) result[0];

        }

        final Function<FastFuture, U> safeJoin = (final FastFuture cf) -> (U) BlockingStreamHelper.getSafe(cf, getErrorHandler());
        final IncrementalReducer<U> collector = new IncrementalReducer(
                                                                       this.getLazyCollector()
                                                                           .get()
                                                                           .withResults(new ArrayList<>()),
                                                                       this);
        final Object[] result = { identity };
        try {
            this.getLastActive()
                .injectFutures()
                .forEach(next -> {

                    collector.getConsumer()
                             .accept(next);
                    result[0] = collector.reduce(safeJoin, (T) result[0], accumulator, combiner);
                });
        } catch (final SimpleReactProcessingException e) {

        }
        return collector.reduceResults(collector.getConsumer()
                                                .getAllResults(),
                                       safeJoin, (T) result[0], accumulator, combiner);
    }

    default <T> T reduce(final T identity, final BiFunction<T, ? super U, T> accumulator) {

        if (getLastActive().isSequential()) {
            final Object[] result = { identity };
            forEach(r -> {
                if (result[0] == null)
                    result[0] = r;
                else {
                    result[0] = accumulator.apply((T) result[0], r);
                }
            });
            return (T) result[0];

        }

        final Function<FastFuture, U> safeJoin = (final FastFuture cf) -> (U) BlockingStreamHelper.getSafe(cf, getErrorHandler());
        final IncrementalReducer<U> collector = new IncrementalReducer(
                                                                       this.getLazyCollector()
                                                                           .get()
                                                                           .withResults(new ArrayList<>()),
                                                                       this);
        final Object[] result = { identity };
        try {
            this.getLastActive()
                .injectFutures()
                .forEach(next -> {

                    collector.getConsumer()
                             .accept(next);
                    result[0] = collector.reduce(safeJoin, (T) result[0], accumulator);
                });
        } catch (final SimpleReactProcessingException e) {

        }
        return collector.reduceResults(collector.getConsumer()
                                                .getAllResults(),
                                       safeJoin, (T) result[0], accumulator);
    }

    default <R> R collect(final Supplier<R> supplier, final BiConsumer<R, ? super U> accumulator, final BiConsumer<R, R> combiner) {
        return (R) this.run((Collector) Collector.of(supplier, accumulator, (a, b) -> {
            combiner.accept(a, b);
            return a;
        }));

    }

}
