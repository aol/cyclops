package com.aol.cyclops2.types.futurestream;

import cyclops.reactive.ReactiveSeq;
import cyclops.async.adapters.Queue;
import cyclops.async.adapters.Queue.QueueReader;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EagerFutureStreamFunctions {
    /**
     * Close all queues except the active one
     * 
     * @param active Queue not to close
     * @param all All queues potentially including the active queue
     */
    static void closeOthers(final Queue active, final List<Queue> all) {
        all.stream()
           .filter(next -> next != active)
           .forEach(Queue::closeAndClear);

    }

    /**
     * Close all streams except the active one
     * 
     * @param active Stream not to close
     * @param all  All streams potentially including the active reactiveStream
     */
    static void closeOthers(final SimpleReactStream active, final List<SimpleReactStream> all) {
        all.stream()
           .filter(next -> next != active)
           .filter(s -> s instanceof BaseSimpleReactStream)
           .forEach(SimpleReactStream::cancel);

    }

    /**
     * Zip two streams into one. Uses the latest values from each rather than waiting for both
     * 
     */
    static <T1, T2> ReactiveSeq<Tuple2<T1, T2>> combineLatest(final SimpleReactStream<T1> left, final SimpleReactStream<T2> right) {
        return combineLatest(left, right, Tuple::tuple);
    }

    /**
     * Return first Stream out of provided Streams that starts emitted results
     * 
     * @param futureStreams Streams to race
     * @return First Stream to skip emitting values
     */
    @SafeVarargs
    public static <U> SimpleReactStream<U> firstOf(final SimpleReactStream<U>... futureStreams) {
        final List<Tuple2<SimpleReactStream<U>, QueueReader>> racers = Stream.of(futureStreams)
                                                                             .map(s -> Tuple.tuple(s, new Queue.QueueReader(
                                                                                                                            s.toQueue(), null)))
                                                                             .collect(Collectors.toList());
        while (true) {
            for (final Tuple2<SimpleReactStream<U>, Queue.QueueReader> q : racers) {
                if (q._2().notEmpty()) {
                    EagerFutureStreamFunctions.closeOthers(q._2().getQueue(), racers.stream()
                                                                                  .map(t -> t._2().getQueue())
                                                                                  .collect(Collectors.toList()));
                    EagerFutureStreamFunctions.closeOthers(q._1(), racers.stream()
                                                                       .map(t -> t._1())
                                                                       .collect(Collectors.toList()));
                    return q._1().fromStream(q._2().getQueue()
                                               .stream(q._1().getSubscription()));
                }

            }
            LockSupport.parkNanos(1l);
        }

    }

    /**
     * Zip two streams into one using a {@link BiFunction} to produce resulting.
     * values. Uses the latest values from each rather than waiting for both.
     * 
     */
    static <T1, T2, R> ReactiveSeq<R> combineLatest(final SimpleReactStream<T1> left, final SimpleReactStream<T2> right, final BiFunction<T1, T2, R> zipper) {

        final Queue q = left.then(it -> new Val(
                                                Val.Pos.left, it))
                            .merge(right.then(it -> new Val(
                                                            Val.Pos.right, it)))
                            .toQueue();
        final Iterator<Val> it = q.stream(left.getSubscription())
                                  .iterator();

        class Zip implements Iterator<R> {
            T1 lastLeft = null;
            T2 lastRight = null;

            @Override
            public boolean hasNext() {

                return it.hasNext();
            }

            @Override
            public R next() {
                final Val v = it.next();
                if (v.pos == Val.Pos.left)
                    lastLeft = (T1) v.val;
                else
                    lastRight = (T2) v.val;

                return zipper.apply(lastLeft, lastRight);

            }
        }

        return ReactiveSeq.fromIterator(new Zip());
    }

    /**
     * Zip two streams into one. Uses the latest values from each rather than waiting for both
     * 
     */
    static <T1, T2> ReactiveSeq<Tuple2<T1, T2>> withLatest(final SimpleReactStream<T1> left, final SimpleReactStream<T2> right) {
        return withLatest(left, right, Tuple::tuple);
    }

    /**
     * Zip two streams into one using a {@link BiFunction} to produce resulting.
     * values. Uses the latest values from each rather than waiting for both.
     * 
     */
    static <T1, T2, R> ReactiveSeq<R> withLatest(final SimpleReactStream<T1> left, final SimpleReactStream<T2> right, final BiFunction<T1, T2, R> zipper) {

        final Queue q = left.then(it -> new Val(
                                                Val.Pos.left, it))
                            .merge(right.then(it -> new Val(
                                                            Val.Pos.right, it)))
                            .toQueue();
        final Iterator<Val> it = q.stream(left.getSubscription())
                                  .iterator();

        class Zip implements Iterator<R> {
            T1 lastLeft = null;
            T2 lastRight = null;

            @Override
            public boolean hasNext() {

                return it.hasNext();
            }

            @Override
            public R next() {
                final Val v = it.next();
                if (v.pos == Val.Pos.left) {
                    lastLeft = (T1) v.val;
                    return zipper.apply(lastLeft, lastRight);
                } else
                    lastRight = (T2) v.val;

                return (R) Optional.empty();

            }
        }

        return ReactiveSeq.fromIterator(new Zip())
                  .filter(next -> !(next instanceof Optional));
    }

    static <T1, T2> ReactiveSeq<T1> skipUntil(final SimpleReactStream<T1> left, final SimpleReactStream<T2> right) {

        final Queue q = left.then(it -> new Val(
                                                Val.Pos.left, it))
                            .merge(right.then(it -> new Val(
                                                            Val.Pos.right, it)))
                            .toQueue();
        final Iterator<Val> it = q.stream(left.getSubscription())
                                  .iterator();

        new Object();
        class Zip implements Iterator<T1> {
            Optional<T1> lastLeft = Optional.empty();
            Optional<T2> lastRight = Optional.empty();

            @Override
            public boolean hasNext() {

                return it.hasNext();
            }

            @Override
            public T1 next() {
                final Val v = it.next();
                if (v.pos == Val.Pos.left) {
                    if (lastRight.isPresent())
                        lastLeft = Optional.of((T1) v.val);
                } else
                    lastRight = Optional.of((T2) v.val);
                if (!lastRight.isPresent())
                    return (T1) Optional.empty();
                if (lastLeft.isPresent())
                    return lastLeft.get();
                else
                    return (T1) Optional.empty();

            }
        }

        return ReactiveSeq.fromIterable(() -> new Zip())
                          .filter(next -> !(next instanceof Optional));
    }

    static <T1, T2> ReactiveSeq<T1> takeUntil(final SimpleReactStream<T1> left, final SimpleReactStream<T2> right) {

        final Queue q = left.then(it -> new Val(
                                                Val.Pos.left, it))
                            .merge(right.then(it -> new Val(
                                                            Val.Pos.right, it)))
                            .toQueue();
        final Iterator<Val> it = q.stream(left.getSubscription())
                                  .iterator();

        new Object();
        class Zip implements Iterator<T1> {
            Optional<T1> lastLeft = Optional.empty();
            Optional<T2> lastRight = Optional.empty();
            boolean closed = false;

            @Override
            public boolean hasNext() {

                return !closed && it.hasNext();
            }

            @Override
            public T1 next() {
                final Val v = it.next();
                if (v.pos == Val.Pos.left)
                    lastLeft = Optional.of((T1) v.val);
                else
                    lastRight = Optional.of((T2) v.val);

                if (!lastRight.isPresent() && lastLeft.isPresent())
                    return lastLeft.get();
                else {
                    closed = true;
                    return (T1) Optional.empty();
                }

            }
        }

        return ReactiveSeq.fromIterable(() -> new Zip())
                          .filter(next -> !(next instanceof Optional));
    }

}
