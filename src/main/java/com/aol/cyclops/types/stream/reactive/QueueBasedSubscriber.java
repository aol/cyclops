package com.aol.cyclops.types.stream.reactive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.async.Queue;
import com.aol.cyclops.data.async.Queue.ClosedQueueException;
import com.aol.cyclops.data.async.QueueFactory;
import com.aol.cyclops.data.collections.extensions.standard.QueueX;
import com.aol.cyclops.react.async.subscription.Continueable;
import com.aol.cyclops.types.futurestream.Continuation;
import com.aol.cyclops.types.futurestream.LazyFutureStream;

import lombok.Getter;
import lombok.Setter;

/**
 * A reactive-streams subscriber for merging data from multiple publishers into a single Stream
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
public class QueueBasedSubscriber<T> implements Subscriber<T> {

    public static <T> QueueBasedSubscriber<T> subscriber(Counter counter, int maxConcurrency) {
        return new QueueBasedSubscriber<>(
                                          counter, maxConcurrency);
    }

    public static <T> QueueBasedSubscriber<T> subscriber(Queue<T> q, Counter counter, int maxConcurrency) {
        return new QueueBasedSubscriber<>(
                                          q, counter, maxConcurrency);
    }

    public static <T> QueueBasedSubscriber<T> subscriber(QueueFactory<T> factory, Counter counter, int maxConcurrency) {

        return new QueueBasedSubscriber<>(
                                          factory, counter, maxConcurrency);
    }

    private Stream<T> genJdkStream() {
        Continueable subscription = new com.aol.cyclops.react.async.subscription.Subscription();
        return queue.stream(subscription);
    }

    private LazyFutureStream<T> genStream() {
        Continueable subscription = new com.aol.cyclops.react.async.subscription.Subscription();
        return LazyFutureStream.of()
                               .withSubscription(subscription)
                               .fromStream(queue.stream(subscription));
    }

    private final int maxConcurrency;
    private final QueueFactory<T> factory;
    @Getter
    protected volatile Queue<T> queue;
    @Getter
    volatile Subscription subscription;

    private volatile LazyFutureStream<T> stream;
    private volatile Supplier<LazyFutureStream<T>> futureStream = Eval.later(this::genStream);
    private volatile Supplier<Stream<T>> jdkStream = Eval.later(this::genJdkStream);
    private volatile Supplier<ReactiveSeq<T>> reactiveSeq = Eval.later(() -> ReactiveSeq.fromStream(jdkStream.get()));
    @Setter
    private volatile Consumer<Throwable> errorHandler;

    private final Counter counter;

    public QueueBasedSubscriber(Counter counter, int maxConcurrency) {
        this.maxConcurrency = maxConcurrency;
        factory = null;

        this.counter = counter;
        queue = new Queue<T>() {
            public T get() {
                counter.subscription.forEach(s -> s.request(1));

                return (T) super.get();
            }
        };
    }

    private QueueBasedSubscriber(Queue<T> q, Counter counter, int maxConcurrency) {
        factory = null;
        this.maxConcurrency = maxConcurrency;
        this.counter = counter;
        queue = q;
    }

    private QueueBasedSubscriber(QueueFactory<T> factory, Counter counter, int maxConcurrency) {
        this.counter = counter;
        this.factory = factory;
        this.maxConcurrency = maxConcurrency;
        this.queue = new Queue<T>(
                                  factory) {
            public T get() {
                counter.subscription.forEach(s -> s.request(1));

                return (T) super.get();
            }
        };

    }

    public LazyFutureStream<T> futureStream() {
        return stream = futureStream.get();
    }

    public Stream<T> jdkStream() {
        return jdkStream.get();
    }

    public ReactiveSeq<T> reactiveSeq() {
        return reactiveSeq.get();
    }

    @Override
    public void onSubscribe(final Subscription s) {
        Objects.requireNonNull(s);

        if (this.subscription != null) {

            subscription.cancel();
            s.cancel();

            return;
        }

        subscription = s;

        while (counter.subscription.size() > maxConcurrency) {

            LockSupport.parkNanos(100l); //max of 10k active subscriptions
        }
        counter.subscription.plus(subscription);

        s.request(1);

    }

    @Override
    public void onNext(T t) {

        Objects.requireNonNull(t);
        queue.add(t);
        counter.added++;

    }

    @Override
    public void onError(Throwable t) {

        Objects.requireNonNull(t);
        if (stream != null)
            ((Consumer) stream.getErrorHandler()
                              .orElse((Consumer) h -> {
                              })).accept(t);
        if (errorHandler != null)
            errorHandler.accept(t);

    }

    public static class Counter {
        public AtomicLong active = new AtomicLong(
                                                  0);
        volatile boolean completable = false;
        final QueueX<Subscription> subscription = QueueX.fromIterable(Collectors.toCollection(() -> new ConcurrentLinkedQueue<Subscription>()),
                                                                      Arrays.<Subscription> asList());
        volatile boolean closed = false;
        volatile int added = 0;
    }

    @Override
    public void onComplete() {

        counter.active.decrementAndGet();
        counter.subscription.minus(subscription);
        boolean set = false;

        if (queue != null && counter.active.get() == 0) {

            if (counter.completable) {

                counter.closed = true;
                queue.addContinuation(new Continuation(
                                                       () -> {
                                                           List current = new ArrayList();
                                                           while (queue.size() > 0)
                                                               current.add(queue.get());
                                                           throw new ClosedQueueException(
                                                                                          current);
                                                       }));
                queue.close();
            }

        }

    }

    public void close() {
        counter.completable = true;
        boolean set = false;

        if (queue != null && counter.active.get() == 0) {
            counter.closed = true;

            queue.addContinuation(new Continuation(
                                                   () -> {
                                                       throw new ClosedQueueException();
                                                   }));
            queue.close();
        }

    }

    public void addContinuation(Continuation c) {
        queue.addContinuation(c);
    }

}
