package com.oath.cyclops.types.reactive;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.oath.cyclops.internal.react.exceptions.SimpleReactProcessingException;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.oath.cyclops.async.adapters.Queue;
import com.oath.cyclops.internal.react.stream.LazyStreamWrapper;

/**
 * Reactive Streams publisher, that publishes on the calling thread
 *
 * @author johnmcclean
 *
 * @param <T> Type of publisher
 */
public interface FutureStreamSynchronousPublisher<T> extends Publisher<T> {
    LazyStreamWrapper getLastActive();

    void cancel();

    void forwardErrors(Consumer<Throwable> c);



    /* (non-Javadoc)
     * @see org.reactivestreams.Publisher#forEachAsync(org.reactivestreams.Subscriber)
     */
    @Override
    default void subscribe(final Subscriber<? super T> s) {

        try {

            forwardErrors(t -> s.onError(t));

            final Queue<T> queue = toQueue();
            final Iterator<CompletableFuture<T>> it = queue.streamCompletableFutures()
                                                           .iterator();

            final Subscription sub = new Subscription() {

                volatile boolean complete = false;

                volatile boolean cancelled = false;
                final LinkedList<Long> requests = new LinkedList<Long>();

                boolean active = false;

                private void handleNext(final T data) {
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

                    final List<CompletableFuture> results = new ArrayList<>();
                    if (active) {

                        return;
                    }
                    active = true;

                    try {

                        while (!cancelled && requests.size() > 0) {
                            final long n2 = requests.peek();

                            for (int i = 0; i < n2; i++) {
                                try {

                                    if (it.hasNext()) {
                                        handleNext(s, it, results);

                                    } else {
                                        handleComplete(results, s);
                                        break;
                                    }
                                } catch (final Throwable t) {
                                    s.onError(t);
                                }

                            }
                            requests.pop();
                        }

                    } finally {
                        active = false;
                    }

                }

                private void handleComplete(final List<CompletableFuture> results, final Subscriber<? super T> s) {
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

                private void callOnComplete(final Subscriber<? super T> s) {

                    s.onComplete();
                }

                private void handleNext(final Subscriber<? super T> s, final Iterator<CompletableFuture<T>> it,
                        final List<CompletableFuture> results) {

                    results.add(it.next()
                                  .thenAccept(r -> {
                        s.onNext(r);
                    })
                                  .exceptionally(t -> {

                        s.onError(t);
                        return null;

                    }));
                    final List<CompletableFuture> newResults = results.stream()
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

        } catch (final SimpleReactProcessingException e) {

        }

    }

    /**
     * @return An async transfer Queue from which to recieve this Publishers data
     */
    Queue<T> toQueue();

}
