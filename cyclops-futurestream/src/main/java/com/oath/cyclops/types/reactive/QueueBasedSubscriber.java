package com.oath.cyclops.types.reactive;

import com.oath.cyclops.react.async.subscription.Continueable;
import com.oath.cyclops.types.futurestream.Continuation;
import cyclops.control.Eval;
import cyclops.futurestream.LazyReact;
import com.oath.cyclops.async.adapters.Queue;
import com.oath.cyclops.async.adapters.Queue.ClosedQueueException;
import com.oath.cyclops.async.adapters.QueueFactory;
import cyclops.futurestream.FutureStream;
import cyclops.reactive.ReactiveSeq;
import lombok.Getter;
import lombok.Setter;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A reactive-streams reactiveSubscriber, backed by a cyclops2-react async.Queue, for merging data from multiple publishers into a single Stream
 *
 * @author johnmcclean
 *
 * @param <T> Subscriber type
 */
public class QueueBasedSubscriber<T> implements Subscriber<T> {

    /**
     * Create a QueueBasedSubscriber, backed by a JDK LinkedBlockingQueue
     *
     * @param counter Counter for tracking connections to the queue and data volumes
     * @param maxConcurrency Maximum number of subscriptions
     * @return QueueBasedSubscriber
     */
    public static <T> QueueBasedSubscriber<T> subscriber(final Counter counter, final int maxConcurrency) {
        return new QueueBasedSubscriber<>(
                counter, maxConcurrency);
    }

    /**
     * Create a QueueBasedSubscriber, backed by the provided Queue
     *
     * @param q Queue backing the reactiveSubscriber
     * @param counter Counter for tracking connections to the queue and data volumes
     * @param maxConcurrency Maximum number of subscriptions
     * @return QueueBasedSubscriber
     */
    public static <T> QueueBasedSubscriber<T> subscriber(final Queue<T> q, final Counter counter, final int maxConcurrency) {
        return new QueueBasedSubscriber<>(
                q, counter, maxConcurrency);
    }

    /**
     * Create a QueueBasedSubscriber, backed by a Queue that will be created with the provided QueueFactory
     *
     * @param factory QueueFactory
     * @param counter Counter for tracking connections to the queue and data volumes
     * @param maxConcurrency Maximum number of subscriptions
     * @return QueueBasedSubscriber
     */
    public static <T> QueueBasedSubscriber<T> subscriber(final QueueFactory<T> factory, final Counter counter, final int maxConcurrency) {

        return new QueueBasedSubscriber<>(
                factory, counter, maxConcurrency);
    }

    private Stream<T> genJdkStream() {
        final Continueable subscription = new com.oath.cyclops.react.async.subscription.Subscription();
        return queue.stream(subscription);
    }

    private FutureStream<T> genStream() {
        final Continueable subscription = new com.oath.cyclops.react.async.subscription.Subscription();
        return new LazyReact().of()
                .withSubscription(subscription)
                .fromStream(queue.stream(subscription));
    }

    private final int maxConcurrency;
    private final QueueFactory<T> factory;
    @Getter
    protected volatile Queue<T> queue;
    @Getter
    volatile Subscription subscription;

    private volatile FutureStream<T> stream;
    private volatile Supplier<FutureStream<T>> futureStream = Eval.later(this::genStream);
    private volatile Supplier<Stream<T>> jdkStream = Eval.later(this::genJdkStream);
    private volatile Supplier<ReactiveSeq<T>> reactiveSeq = Eval.later(() -> ReactiveSeq.fromStream(jdkStream.get()));
    @Setter
    private volatile Consumer<Throwable> errorHandler;

    private final Counter counter;

    public QueueBasedSubscriber(final Counter counter, final int maxConcurrency) {
        this.maxConcurrency = maxConcurrency;
        factory = null;

        this.counter = counter;
        queue = new Queue<T>() {
            @Override
            public T get() {
                counter.subscription.forEach(s -> s.request(1));

                return super.get();
            }
        };
    }

    private QueueBasedSubscriber(final Queue<T> q, final Counter counter, final int maxConcurrency) {
        factory = null;
        this.maxConcurrency = maxConcurrency;
        this.counter = counter;
        queue = q;
    }

    private QueueBasedSubscriber(final QueueFactory<T> factory, final Counter counter, final int maxConcurrency) {
        this.counter = counter;
        this.factory = factory;
        this.maxConcurrency = maxConcurrency;
        this.queue = new Queue<T>(
                factory) {
            @Override
            public T get() {

                if(size()<maxConcurrency*3 && counter.subscription.size()>0) {
                    counter.subscription.forEach(s -> s.request(1));
                }
                    T res = super.get();
                    return res;

            }
        };

    }

    /**
     * @return FutureStream generated from this QueueBasedSubscriber
     */
    public FutureStream<T> futureStream() {
        return stream = futureStream.get();
    }

    /**
     * @return JDK Stream generated from this QueueBasedSubscriber
     */
    public Stream<T> jdkStream() {
        return jdkStream.get();
    }

    /**
     * @return ReactiveSeq generated from this QueueBasedSubscriber
     */
    public ReactiveSeq<T> reactiveSeq() {
        return reactiveSeq.get();
    }

    /* (non-Javadoc)
     * @see org.reactivestreams.Subscriber#onSubscribe(org.reactivestreams.Subscription)
     */
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


            LockSupport.parkNanos(100l);
        }

        counter.subscription.add(subscription);


        s.request(1);

    }



    /* (non-Javadoc)
     * @see org.reactivestreams.Subscriber#onNext(java.lang.Object)
     */
    @Override
    public void onNext(final T t) {

        Objects.requireNonNull(t);
        counter.added++;
        queue.add(t);





    }

    /* (non-Javadoc)
     * @see org.reactivestreams.Subscriber#onError(java.lang.Throwable)
     */
    @Override
    public void onError(final Throwable t) {

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
        public volatile boolean completable = false;
        public final ConcurrentLinkedQueue<Subscription> subscription =  new ConcurrentLinkedQueue<Subscription>();
        volatile boolean closed = false;
        public volatile int added = 0;
        final AtomicBoolean closing =new AtomicBoolean(false);
    }

    /* (non-Javadoc)
     * @see org.reactivestreams.Subscriber#onComplete()
     */
    @Override
    public void onComplete() {


        counter.active.decrementAndGet();
        counter.subscription.remove(subscription);
        if (queue != null && counter.active.get() == 0) {

            if (counter.completable) {

                if(counter.closing.compareAndSet(false,true)) {
                    counter.closed = true;
                    queue.addContinuation(new Continuation(
                            () -> {
                                final List current = new ArrayList();
                                while (queue.size() > 0) {
                                    try {

                                        current.add(queue.get());
                                    }catch(ClosedQueueException e){

                                        break;
                                    }
                                }

                                throw new ClosedQueueException(
                                        current);
                            }));

                    queue.close();
                }
            }

        }

    }

    public void close() {

        counter.completable = true;

        if (queue != null && counter.active.get() == 0) {
            if(counter.closing.compareAndSet(false,true)) {
                counter.closed = true;

                queue.addContinuation(new Continuation(
                        () -> {

                            throw new ClosedQueueException();
                        }));

                queue.close();
            }
        }


    }

    public void addContinuation(final Continuation c) {
        queue.addContinuation(c);
    }

}
