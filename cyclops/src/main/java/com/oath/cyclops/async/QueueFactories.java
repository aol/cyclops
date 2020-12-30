package com.oath.cyclops.async;

import com.oath.cyclops.async.adapters.Queue;
import com.oath.cyclops.async.adapters.QueueFactory;
import com.oath.cyclops.async.wait.NoWaitRetry;
import com.oath.cyclops.async.wait.WaitStrategy;
import org.agrona.concurrent.ManyToOneConcurrentArrayQueue;
import org.agrona.concurrent.OneToOneConcurrentArrayQueue;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * Methods for generating QueueFactories for plumbing Streams together
 *
 *
 * <pre>
 * {@code
 *    Queue<String> transferQueue = QueueFactories.<String>boundedQueue(4)
                                                 .build();

      new LazyReact(Executors.newFixedThreadPool(4)).generate(()->"data")
                                                      .map(d->"emitted on " + Thread.currentThread().getId())
                                                      .peek(System.out::println)
                                                      .peek(d->transferQueue.offer(d))
                                                      .run();


       transferQueue.stream()
                    .map(e->"Consumed on " + Thread.currentThread().getId())
                     .futureOperations(Executors.newFixedThreadPool(1))
                     .forEach(System.out::println);
 *
 *
 * }
 * </pre>
 *
 * @author johnmcclean
 *
 */
public class QueueFactories {

    /**
     * Create a QueueFactory for boundedQueues where bound is determined by the provided queueSize parameter
     * Generated Queues will be backed by a LinkedBlockingQueue
     *
     * <pre>
     * {@code
     *   Queue<String> transferQueue = QueueFactories.<String>boundedQueue(4)
                                                 .build();

         new LazyReact(Executors.newFixedThreadPool(4)).generate(()->"data")
                                                      .map(d->"emitted on " + Thread.currentThread().getId())
                                                      .peek(System.out::println)
                                                      .peek(d->transferQueue.offer(d))
                                                      .run();


         transferQueue.stream()
                      .map(e->"Consumed on " + Thread.currentThread().getId())
                      .futureOperations(Executors.newFixedThreadPool(1))
                      .forEach(System.out::println);
     *
     * }
     * </pre>
     *
     * @param queueSize  Max queue size
     * @return QueueFactory for bounded Queues backed by a LinkedBlockingQueue
     */
    public static <T> QueueFactory<T> boundedQueue(final int queueSize) {
        return () -> new Queue<T>(
                                  new LinkedBlockingQueue<>(
                                                            queueSize));
    }

    /**
     * <pre>
     * {@code
     *   ReactiveSeq.of(1,2,3)
                    .flatMapP(i->ReactiveSeq.range(i,1500),1000,QueueFactories.unboundedQueue())
                    .listX()
     * }
     * </pre>
     *
     * @return A QueueFactory for unbounded Queues backed by a LinkedBlockingQueue
     */
    public static <T> QueueFactory<T> unboundedQueue() {
        return () -> new Queue<T>();
    }

    /**
     * Creates an async.Queue backed by a JDK Wait Free unbounded ConcurrentLinkedQueue
     * Wait strategy used is NoWaitRetry by default for both Consumers and Producers
     * (both Consumers and Producers will repeatedly retry until successful). Use
     *  withConsumerWaitStrategy &amp; withProducerWaitStrategy methods on the returned queue to change the
     *  wait strategy
     * <pre>
     * {@code
     *    queue.withConsumerWaitStrategy(new DirectWaitStrategy())
     *         .withProducerWaitStrategy(new YieldWait());
     * }</pre>
     *
     *
     * @return Factory for unbounded wait free queue backed by ConcurrentLinkedQueue
     */
    public static <T> QueueFactory<T> unboundedNonBlockingQueue() {
        return () -> new Queue<T>(
                                  new ConcurrentLinkedQueue<>(), new NoWaitRetry<>(), new NoWaitRetry<>());

    }

    /**
     * Creates an async.Queue backed by a JDK Wait Free unbounded ConcurrentLinkedQueue
     * The provided WaitStrategy is used to determine behaviour of both producers and consumers when the Queue is full (producer)
     * or zero (consumer). {@see WaitStrategy#spinWait() , @see WaitStrategy#exponentialBackOff() , @see WaitStrategy#noWaitRetry() }
     *
     * @param strategy Strategy to be employed by producers when Queue is full, or consumers when Queue is zero
     * @return Factory for unbounded wait free queue backed by ConcurrentLinkedQueue
     */
    public static <T> QueueFactory<T> unboundedNonBlockingQueue(final WaitStrategy<T> strategy) {
        return () -> new Queue<T>(
                                  new ConcurrentLinkedQueue<>(), strategy, strategy);

    }

    /**
     * Creates an async.Queue backed by an Agrona ManyToOneConcurrentArrayQueue bounded by specified queueSize
     *  Wait strategy used is NoWaitRetry by default for both Consumers and Producers
     *  (both Consumers and Producers will repeatedly retry until successful). Use
     *  withConsumerWaitStrategy  &amp; withProducerWaitStrategy methods on the returned queue to change the
     *  wait strategy
     * <pre>
     * {@code
     *    queue.withConsumerWaitStrategy(new DirectWaitStrategy())
     *         .withProducerWaitStrategy(new YieldWait());
     * }</pre>
     *
     * @param queueSize upper bound for Queue
     * @return bounded wait free Queue Factory backed by an Agrona ManyToOneConcurrentArrayQueue
     */
    public static <T> QueueFactory<T> boundedNonBlockingQueue(final int queueSize) {
        return () -> new Queue<T>(
                                  new ManyToOneConcurrentArrayQueue<>(
                                                                      queueSize),
                                  new NoWaitRetry<>(), new NoWaitRetry<>());
    }

    /**
     * Generate QueueFactory for bounded non blocking queues. Max queue size is determined by the input parameter.
     * The provided WaitStrategy is used to determine behaviour of both producers and consumers when the Queue is full (producer)
     * or zero (consumer). {@see WaitStrategy#spinWait() , @see WaitStrategy#exponentialBackOff() , @see WaitStrategy#noWaitRetry() }
     *
     * @param queueSize Max Queue size
     * @param strategy Strategy to be employed by producers when Queue is full, or consumers when Queue is zero
     * @return bounded wait free Queue Factory backed by an Agrona ManyToOneConcurrentArrayQueue
     */
    public static <T> QueueFactory<T> boundedNonBlockingQueue(final int queueSize, final WaitStrategy<T> strategy) {
        return () -> new Queue<T>(
                                  new ManyToOneConcurrentArrayQueue<>(
                                                                      queueSize),
                                  strategy, strategy);
    }

    /**
     * Creates an async.Queue backed by an Agrona OneToOneConcurrentArrayQueue bounded by specified queueSize
     *  Wait strategy used is NoWaitRetry by default for both Consumers and Producers
     *  (both Consumers and Producers will repeatedly retry until successful). Use
     *  withConsumerWaitStrategy  &amp; withProducerWaitStrategy methods on the returned queue to change the
     *  wait strategy
     * <pre>
     * {@code
     *    queue.withConsumerWaitStrategy(new DirectWaitStrategy())
     *         .withProducerWaitStrategy(new YieldWait());
     * }</pre>
     *
     * @param queueSize
     * @return
     */
    public static <T> QueueFactory<T> singleWriterboundedNonBlockingQueue(final int queueSize) {
        return () -> new Queue<T>(
                                  new OneToOneConcurrentArrayQueue<>(
                                                                     queueSize),
                                  new NoWaitRetry<>(), new NoWaitRetry<>());

    }
    /**
     * Generate QueueFactory for bounded non blocking queues. Max queue size is determined by the input parameter.
     * The provided WaitStrategy is used to determine behaviour of both producers and consumers when the Queue is full (producer)
     * or zero (consumer). {@see WaitStrategy#spinWait() , @see WaitStrategy#exponentialBackOff() , @see WaitStrategy#noWaitRetry() }
     *
     * @param queueSize Max Queue size
     * @param strategy Strategy to be employed by producers when Queue is full, or consumers when Queue is zero
     * @return bounded wait free Queue Factory backed by an Agrona OneToOneConcurrentArrayQueue
     */
    public static <T> QueueFactory<T> singleWriterboundedNonBlockingQueue(final int queueSize, final WaitStrategy<T> strategy) {
        return () -> new Queue<T>(
                                  new OneToOneConcurrentArrayQueue<>(
                                                                     queueSize),
                                  strategy, strategy);

    }

    /**
     * @return async.Queue backed by a Synchronous Queue
     */
    public static <T> QueueFactory<T> synchronousQueue() {
        return () -> new Queue<T>(
                                  new SynchronousQueue<>());
    }

}
