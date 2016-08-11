package com.aol.cyclops.types.stream.reactive;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.aol.cyclops.data.async.Queue;
import com.aol.cyclops.internal.react.exceptions.SimpleReactProcessingException;
import com.aol.cyclops.internal.react.stream.LazyStreamWrapper;

/**
 * Reactive Streams publisher, that publishes on the calling thread
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
public interface FutureStreamSynchronousPublisher<T> extends Publisher<T> {
    LazyStreamWrapper getLastActive();

    void cancel();

    void forwardErrors(Consumer<Throwable> c);

    default void subscribeSync(Subscriber<? super T> s) {
        FutureStreamSynchronousPublisher.this.subscribe(s);
    }

    default void subscribe(Subscriber<? super T> s) {

        try {

            forwardErrors(t -> s.onError(t));

            Queue<T> queue = toQueue();
            Iterator<CompletableFuture<T>> it = queue.streamCompletableFutures()
                                                     .iterator();

            Subscription sub = new Subscription() {

                volatile boolean complete = false;

                volatile boolean cancelled = false;
                final LinkedList<Long> requests = new LinkedList<Long>();

                boolean active = false;

                private void handleNext(T data) {
                    if (!cancelled) {
                        s.onNext(data);
                    }

                }

                @Override
                public void request(final long n) {

                    if (n < 1) {
                        s.onError(new IllegalArgumentException(
                                                               "3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                    }
                    requests.add(n);

                    List<CompletableFuture> results = new ArrayList<>();
                    if (active) {

                        return;
                    }
                    active = true;

                    try {

                        while (!cancelled && requests.size() > 0) {
                            long n2 = requests.peek();

                            for (int i = 0; i < n2; i++) {
                                try {

                                    if (it.hasNext()) {
                                        handleNext(s, it, results);

                                    } else {
                                        handleComplete(results, s);
                                        break;
                                    }
                                } catch (Throwable t) {
                                    s.onError(t);
                                }

                            }
                            requests.pop();
                        }

                    } finally {
                        active = false;
                    }

                }

                private void handleComplete(List<CompletableFuture> results, Subscriber<? super T> s) {
                    if (!complete && !cancelled) {
                        complete = true;

                        if (results.size() > 0) {
                            CompletableFuture.allOf(results.stream()
                                                           .map(cf -> cf.exceptionally(e -> null))
                                                           .collect(Collectors.toList())
                                                           .toArray(new CompletableFuture[results.size()]))
                                             .thenAccept(a -> callOnComplete(s))
                                             .exceptionally(e -> {
                                callOnComplete(s);
                                return null;
                            });
                        } else {
                            callOnComplete(s);
                        }

                    }
                }

                private void callOnComplete(Subscriber<? super T> s) {

                    s.onComplete();
                }

                private void handleNext(Subscriber<? super T> s, Iterator<CompletableFuture<T>> it, List<CompletableFuture> results) {

                    results.add(it.next()
                                  .thenAccept(r -> {
                        s.onNext(r);
                    })
                                  .exceptionally(t -> {

                        s.onError(t);
                        return null;

                    }));
                    List<CompletableFuture> newResults = results.stream()
                                                                .filter(cf -> cf.isDone())
                                                                .collect(Collectors.toList());
                    results.removeAll(newResults);
                }

                @Override
                public void cancel() {

                    cancelled = true;
                    forwardErrors(t -> {
                    });
                    queue.closeAndClear();

                }

            };
            s.onSubscribe(sub);

        } catch (SimpleReactProcessingException e) {

        }

    }

    Queue<T> toQueue();

}