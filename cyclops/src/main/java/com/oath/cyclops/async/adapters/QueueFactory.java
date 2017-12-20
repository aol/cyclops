package com.oath.cyclops.async.adapters;

/**
 * Interface for Factories of async.Queues
 * {@see QueueFactories}
 *
 * <pre>
 * {@code
 *
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
 * @author johnmcclean
 *
 * @param <T> Data type for elements stored within the generated async.Queue
 */
public interface QueueFactory<T> {

    /**
     * Build an async.Queue using this factory
     *
     * @return async.Queue
     */
    public Queue<T> build();
}
