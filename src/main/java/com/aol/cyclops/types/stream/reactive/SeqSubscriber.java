package com.aol.cyclops.types.stream.reactive;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.aol.cyclops.types.stream.ConvertableSequence;
import com.aol.cyclops.util.ExceptionSoftener;

import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import lombok.AccessLevel;

/**
 * A reactive-streams Subscriber that can generate various forms of sequences from a publisher
 * 
 * <pre>
 * {@code 
 *    SeqSubscriber<Integer> ints = SeqSubscriber.subscriber();
 *    ReactiveSeq.of(1,2,3)
 *               .publish(ints);
 *    
 *   ListX list = ints.toListX();
 * }
 * </pre>
 * 
 * @author johnmcclean
 *
 * @param <T> Subscriber type
 */
@AllArgsConstructor(access=AccessLevel.PRIVATE)
public class SeqSubscriber<T> implements Subscriber<T>, Supplier<T>, ConvertableSequence<T> {

    private final Object UNSET = new Object();
    private final AtomicReference lastValue = new AtomicReference(
                                                                  UNSET);
    private final AtomicReference lastError = new AtomicReference(
                                                                  UNSET);
    private final Runnable onComplete;
    private volatile boolean complete = false;
    private volatile boolean unread = false;
    private volatile Subscription s;
    
    @Wither
    private final long spinNanos;

    protected SeqSubscriber() {
        this.onComplete = () -> {
        };
        this.spinNanos = 1000000l;
    }

    private SeqSubscriber(final Runnable onComplete) {
        super();
        this.onComplete = onComplete;
        this.spinNanos =1000000l;
    }

    public static <T> SeqSubscriber<T> subscriber(final Runnable onComplete) {
        return new SeqSubscriber<>(
                                   onComplete);
    }

    public static <T> SeqSubscriber<T> subscriber() {

        return new SeqSubscriber<>(
                                   () -> {
                                   });
    }

    @Override
    public void onSubscribe(final Subscription s) {
        Objects.requireNonNull(s);
        if (this.s == null) {
            this.s = s;
            s.request(1);
        } else
            s.cancel();

    }

    @Override
    public void onNext(final T t) {
        unread = true;
        Objects.requireNonNull(t);
        lastValue.set(t);
    }

    @Override
    public void onError(final Throwable t) {
        Objects.requireNonNull(t);
        lastError.set(t);
    }

    @Override
    public void onComplete() {
        complete = true;
        this.onComplete.run();

    }

    @Override
    public T get() {
        try {
            while (lastValue.get() == UNSET && lastError.get() == UNSET)
                LockSupport.parkNanos(this.spinNanos);
            if (lastError.get() != UNSET) {
                final Throwable toThrow = (Throwable) lastError.get();
                reset();

                throw ExceptionSoftener.throwSoftenedException(toThrow);
            }
            final T result = (T) lastValue.get();

            return result;
        } finally {
            unread = false;
        }

    }

    private void reset() {
        lastValue.set(UNSET);
        lastError.set(UNSET);
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            boolean requested = true;
            Object next = complete ? !unread ? UNSET : get() : get();

            @Override
            public boolean hasNext() {
                if (!requested) {
                    reset();
                    s.request(1l);
                    requested = true;
                    if (unread)
                        next = get();
                    else
                        next = UNSET;

                }
                return next != UNSET;
            }

            @Override
            public T next() {
                if (!requested) {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                }
                if (next == UNSET)
                    throw new NoSuchElementException();
                requested = false;
                return (T) next;
            }

        };
    }



    @Override
    public Spliterator<T> spliterator() {
        return new Spliterator<T>() {
            boolean requested = true;

            @Override
            public boolean tryAdvance(final Consumer<? super T> action) {
                if (!requested)
                    s.request(1l);
                else
                    requested = false;
                final Object next = complete ? !unread ? UNSET : get() : get();

                if (next != UNSET) {
                    action.accept((T) next);
                    return true;
                }
                return false;

            }

            @Override
            public Spliterator<T> trySplit() {
                return this;
            }

            @Override
            public long estimateSize() {
                return Long.MAX_VALUE;
            }

            @Override
            public int characteristics() {
                return IMMUTABLE;
            }

        };

    }

}
