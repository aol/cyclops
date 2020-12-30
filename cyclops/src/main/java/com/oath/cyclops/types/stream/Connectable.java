package com.oath.cyclops.types.stream;

import cyclops.reactive.ReactiveSeq;
import org.agrona.concurrent.OneToOneConcurrentArrayQueue;

import java.util.Queue;
import java.util.function.Function;

/**
 * A Connectable - Stream already emitting data
 *
 * <pre>
 * {@code
 *   ReactiveSeq.range(1,1_000_000_000)
                .hotStream(exec)
                .connect()
                .forEach(System.out::println)
 *
 * }
 * </pre>
 *
 *
 * @author johnmcclean
 *
 * @param <T> Data type of elements in the Stream
 */
public interface Connectable<T> {

    /**
     * Connect to this Connectable (Stream that is already emitting data)
     *
     * <pre>
     * {@code
     *
     * ReactiveSeq.range(0,Integer.MAX_VALUE)
                    .limit(100)
                    .peek(v->value=v)
                    .peek(v->latch.countDown())
                    .peek(System.out::println)
                    .hotStream(exec)
                    .connect()
                    .limit(100)
                    .futureOperations(ForkJoinPool.commonPool())
                    .forEach(System.out::println)
     *
     * }
     * </pre>
     *
     *
     * @return Stream connected to the Connectable emitting data
     */
    public default ReactiveSeq<T> connect() {
        return connect(new OneToOneConcurrentArrayQueue<T>(
                                                           256));
    }

    /**
     * Connect to this Connectable using the provided transfer async.Queue.
     * The transfer Queue can be used to applyHKT backpressure to the Connectable if it produces
     * data faster than the connected Stream can consume it {@see cyclops2.async.wait.WaitStrategy}
     *
     * <pre>
     * {@code
     * new LazyReact().range(0,Integer.MAX_VALUE)
                .limit(1000)
                .peek(v->value=v)
                .peek(v->latch.countDown())
                .hotStream(exec)
                .connect(new LinkedBlockingQueue<>())
                .limit(100)
                .futureOperations(ForkJoinPool.commonPool())
                .forEach(System.out::println)
     *
     *
     *
     * }
     * </pre>
     *
     *
     *
     * @param queue Transfer Queue between the Streams
     * @return Stream connected to the Connectable emitting data
     */
    public ReactiveSeq<T> connect(Queue<T> queue);

    /**
     * Connect to this Connectable using the provided transfer async.Queue.
     * The transfer Queue can be used to apply backpressure to the Connectable if it produces
     * data faster than the connected Stream can consume it {@see cyclops2.async.wait.WaitStrategy}
     * Convert the emitted Stream to the required type with the provided function
     *
     * @param queue  Transfer Queue between the Streams
     * @param to Function to convert a ReactiveSeq to desired Stream type
     * @return Stream connected to the Connectable emitting data
     */
    public default <R> R connectTo(final Queue<T> queue, final Function<? super ReactiveSeq<T>, ? extends R> to) {
        return to.apply(connect(queue));
    }
}
