package com.aol.cyclops2.internal.react.stream;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cyclops.async.SimpleReact;
import com.aol.cyclops2.types.futurestream.BlockingStreamHelper;
import com.aol.cyclops2.types.futurestream.SimpleReactStream;
import com.aol.cyclops2.util.ExceptionSoftener;

import lombok.AllArgsConstructor;
import lombok.experimental.Wither;

@Wither
@AllArgsConstructor
public class EagerStreamWrapper implements StreamWrapper {
    @SuppressWarnings("rawtypes")
    private final List<CompletableFuture> list;
    private final Stream<CompletableFuture> stream;
    private final AsyncList async;
    private final Optional<Consumer<Throwable>> errorHandler;

    public EagerStreamWrapper(final List<CompletableFuture> list, final Optional<Consumer<Throwable>> errorHandler) {
        this.list = list;
        stream = null;
        this.errorHandler = errorHandler;
        async = null;
    }

    public EagerStreamWrapper(final AsyncList async, final Optional<Consumer<Throwable>> errorHandler) {
        list = null;
        stream = null;
        this.async = async;
        this.errorHandler = errorHandler;
    }

    public EagerStreamWrapper(final Stream<CompletableFuture> stream, final Optional<Consumer<Throwable>> errorHandler) {
        this.stream = stream;

        list = stream.collect(Collectors.toList());
        this.errorHandler = errorHandler;
        async = null;

    }

    public EagerStreamWrapper(final Stream<CompletableFuture> stream, final Collector c, final Optional<Consumer<Throwable>> errorHandler) {
        this.stream = stream;
        async = null;
        this.errorHandler = errorHandler;
        list = (List) stream.collect(c);

    }

    public void collect() {
        if (list != null)
            collect(list.stream(), Collectors.toList(), errorHandler);
        else
            collect(stream, Collectors.toList(), errorHandler);
    }

    static List<CompletableFuture> collect(final Stream<CompletableFuture> stream, final Collector c,
            final Optional<Consumer<Throwable>> errorHandler) {

        final Function<Throwable, Object> captureFn = t -> {
            BlockingStreamHelper.captureUnwrap(t, errorHandler);
            throw ExceptionSoftener.throwSoftenedException(t);
        };
        if (errorHandler.isPresent())
            return (List<CompletableFuture>) stream.map(cf -> cf.exceptionally(captureFn))

                                                   .collect(c);

        return (List<CompletableFuture>) stream.filter(cf -> cf.isCompletedExceptionally())
                                               .collect(c);

    }

    public EagerStreamWrapper(final CompletableFuture cf, final Optional<Consumer<Throwable>> errorHandler) {
        async = null;
        list = Arrays.asList(cf);
        this.errorHandler = errorHandler;
        stream = null;

    }

    public EagerStreamWrapper withNewStream(final Stream<CompletableFuture> stream, final SimpleReact simple) {

        return new EagerStreamWrapper(
                                      new AsyncList(
                                                    stream, simple.getQueueService()),
                                      errorHandler);
    }

    public EagerStreamWrapper stream(final Function<Stream<CompletableFuture>, Stream<CompletableFuture>> action) {
        if (async != null)
            return new EagerStreamWrapper(
                                          async.stream(action), errorHandler);
        else
            return new EagerStreamWrapper(
                                          action.apply(list.stream()), errorHandler);

    }

    @Override
    public Stream<CompletableFuture> stream() {
        if (async != null)
            return async.async.join()
                              .stream();

        return list.stream();

    }

    public List<CompletableFuture> list() {
        if (async != null)
            return async.async.join();

        return list;
    }

    static class AsyncList {

        private final Executor service;
        // = Executors.newSingleThreadExecutor();
        private final CompletableFuture<List<CompletableFuture>> async;

        public AsyncList(final Stream<CompletableFuture> stream, final Executor service) {

            if (stream instanceof SimpleReactStream)
                async = CompletableFuture.completedFuture(stream.collect(Collectors.toList()));
            else
                async = CompletableFuture.supplyAsync(() -> stream.collect(Collectors.toList()), service);

            this.service = service;
        }

        public AsyncList(final CompletableFuture<Stream<CompletableFuture>> cf, final Executor service) {
            // use elastic pool to execute asyn

            async = cf.thenApplyAsync(st -> st.collect(Collectors.toList()), service);
            this.service = service;

        }

        public AsyncList stream(final Function<Stream<CompletableFuture>, Stream<CompletableFuture>> action) {
            return new AsyncList(
                                 async.thenApply(list -> action.apply(list.stream())), service);

        }
    }

}