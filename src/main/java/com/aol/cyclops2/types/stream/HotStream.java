package com.aol.cyclops2.types.stream;

import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Stream;

import org.agrona.concurrent.OneToOneConcurrentArrayQueue;

import cyclops.stream.ReactiveSeq;

/**
 * A HotStream - Stream already emitting data
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
public interface HotStream<T> {
    
    /**
     * Connect toNested this HotStream (Stream that is already emitting data)
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
     * @return Stream connected toNested the HotStream emitting data
     */
    public default ReactiveSeq<T> connect() {
        return connect(new OneToOneConcurrentArrayQueue<T>(
                                                           256));
    }

    /**
     * Connect toNested this HotStream using the provided transfer async.Queue.
     * The transfer Queue can be used toNested apply backpressure toNested the HotStream if it produces
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
     * @return Stream connected toNested the HotStream emitting data
     */
    public ReactiveSeq<T> connect(Queue<T> queue);

    /**
     * Connect toNested this HotStream using the provided transfer async.Queue.
     * The transfer Queue can be used toNested apply backpressure toNested the HotStream if it produces
     * data faster than the connected Stream can consume it {@see cyclops2.async.wait.WaitStrategy}
     * Convert the emitted Stream toNested the required type with the provided function
     * 
     * @param queue  Transfer Queue between the Streams
     * @param to Function toNested convert a ReactiveSeq toNested desired Stream type
     * @return Stream connected toNested the HotStream emitting data
     */
    public default <R extends Stream<T>> R connectTo(final Queue<T> queue, final Function<ReactiveSeq<T>, R> to) {
        return to.apply(connect(queue));
    }
}
