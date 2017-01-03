package com.aol.cyclops2.internal.stream;

import static org.jooq.lambda.tuple.Tuple.tuple;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jooq.lambda.tuple.Tuple3;
import org.reactivestreams.Subscription;

import cyclops.Streams;

public class FutureStreamUtils {
    /**
     * Perform a forEach operation over the Stream, without closing it, consuming only the specified number of elements from
     * the Stream, at this time. More elements can be consumed later, by called request on the returned Subscription
     * 
     * <pre>
     * {@code
     *     Subscription next = Streams.forEach(Stream.of(1,2,3,4),2,System.out::println);
     *          
     *     System.out.println("First batch processed!");
     *     
     *     next.request(2);
     *     
     *      System.out.println("Second batch processed!");
     *      
     *     //prints
     *     1
     *     2
     *     First batch processed!
     *     3
     *     4 
     *     Second batch processed!
     * }
     * </pre>
     * 
     * @param stream - the Stream to consume data from
     * @param x To consume from the Stream at this time
     * @param consumerElement To accept incoming events from the Stream
     * @return Subscription so that further processing can be continued or cancelled.
     */
    public static <T, X extends Throwable> Tuple3<CompletableFuture<Subscription>, Runnable, CompletableFuture<Boolean>> forEachX(
            final Stream<T> stream, final long x, final Consumer<? super T> consumerElement) {
        return forEachXEvents(stream, x, consumerElement, e -> {
        } , () -> {
        });

    }

    /**
     * Perform a forEach operation over the Stream  without closing it,  capturing any elements and errors in the supplied consumers, but only consuming 
     * the specified number of elements from the Stream, at this time. More elements can be consumed later, by called request on the returned Subscription 
     * <pre>
     * {@code
     *     Subscription next = Streams.forEach(Stream.of(()->1,()->2,()->throw new RuntimeException(),()->4)
     *                                  .map(Supplier::get),System.out::println, e->e.printStackTrace());
     *          
     *     System.out.println("First batch processed!");
     *     
     *     next.request(2);
     *     
     *      System.out.println("Second batch processed!");
     *      
     *     //prints
     *     1
     *     2
     *     First batch processed!
     *     
     *     RuntimeException Stack Trace on System.err
     *     
     *     4 
     *     Second batch processed!
     * }
     * </pre>	 
     * 
     * @param stream - the Stream to consume data from
     * @param x To consume from the Stream at this time
     * @param consumerElement To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     * @return Subscription so that further processing can be continued or cancelled.
     */
    public static <T, X extends Throwable> Tuple3<CompletableFuture<Subscription>, Runnable, CompletableFuture<Boolean>> forEachXWithError(
            final Stream<T> stream, final long x, final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError) {
        return forEachXEvents(stream, x, consumerElement, consumerError, () -> {
        });
    }

    /**
     * Perform a forEach operation over the Stream  without closing it,  capturing any elements and errors in the supplied consumers, but only consuming 
     * the specified number of elements from the Stream, at this time. More elements can be consumed later, by called request on the returned Subscription,
     * when the entire Stream has been processed an onComplete event will be recieved.
     * 
     * <pre>
     * {@code
     *     Subscription next = Streams.forEach(Stream.of(()->1,()->2,()->throw new RuntimeException(),()->4)
     *                                  .map(Supplier::get) ,System.out::println, e->e.printStackTrace(),()->System.out.println("the end!"));
     *          
     *     System.out.println("First batch processed!");
     *     
     *     next.request(2);
     *     
     *      System.out.println("Second batch processed!");
     *      
     *     //prints
     *     1
     *     2
     *     First batch processed!
     *     
     *     RuntimeException Stack Trace on System.err
     *     
     *     4 
     *     Second batch processed!
     *     The end!
     * }
     * </pre>
     * @param stream - the Stream to consume data from	 
     * @param x To consume from the Stream at this time
     * @param consumerElement To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     * @param onComplete To run after an onComplete event
     * @return Subscription so that further processing can be continued or cancelled.
     */
    public static <T, X extends Throwable> Tuple3<CompletableFuture<Subscription>, Runnable, CompletableFuture<Boolean>> forEachXEvents(
            final Stream<T> stream, final long x, final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError,
            final Runnable onComplete) {
        final CompletableFuture<Boolean> streamCompleted = new CompletableFuture<>();
        final Subscription s = new Subscription() {
            Iterator<T> it = stream.iterator();
            volatile boolean running = true;

            @Override
            public void request(final long n) {

                for (int i = 0; i < n && running; i++) {
                    try {

                        if (it.hasNext()) {

                            consumerElement.accept(it.next());
                        } else {
                            try {
                                onComplete.run();
                            } finally {
                                streamCompleted.complete(true);
                                break;
                            }
                        }
                    } catch (final Throwable t) {
                        consumerError.accept(t);
                    }
                }
            }

            @Override
            public void cancel() {
                running = false;

            }

        };
        final CompletableFuture<Subscription> subscription = CompletableFuture.completedFuture(s);

        return tuple(subscription, () -> {
            s.request(x);

        } , streamCompleted);

    }

    /**
     *  Perform a forEach operation over the Stream    capturing any elements and errors in the supplied consumers,  
     * <pre>
     * {@code
     *     Subscription next = StreanUtils.forEach(Stream.of(()->1,()->2,()->throw new RuntimeException(),()->4)
     *                                  .map(Supplier::get),System.out::println, e->e.printStackTrace());
     *          
     *     System.out.println("processed!");
     *     
     *    
     *      
     *     //prints
     *     1
     *     2
     *     RuntimeException Stack Trace on System.err
     *     4
     *     processed!
     *     
     * }
     * </pre>
     * @param stream - the Stream to consume data from	 
     * @param consumerElement To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     * @return A Tuple containing a Future with a Subscription to this publisher, a runnable to start processing on a separate thread, and future that stores true / false depending on success
     */
    public static <T, X extends Throwable> Tuple3<CompletableFuture<Subscription>, Runnable, CompletableFuture<Boolean>> forEachWithError(
            final Stream<T> stream, final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError) {
        return forEachEvent(stream, consumerElement, consumerError, () -> {
        });

    }

    /**
     * Perform a forEach operation over the Stream  capturing any elements and errors in the supplied consumers
     * when the entire Stream has been processed an onComplete event will be recieved.
     * 
     * <pre>
     * {@code
     *     Subscription next = Streams.forEachEvents(Stream.of(()->1,()->2,()->throw new RuntimeException(),()->4)
     *                                  .map(Supplier::get),System.out::println, e->e.printStackTrace(),()->System.out.println("the end!"));
     *          
     *     System.out.println("processed!");
     *     
     *      
     *     //prints
     *     1
     *     2
     *     RuntimeException Stack Trace on System.err
     *      4 
     *     processed!
     *     
     *     
     * }
     * </pre>
     * @param stream - the Stream to consume data from	
     * @param consumerElement To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     * @param onComplete To run after an onComplete event
     * @return A Tuple containing a Future with a Subscription to this publisher, a runnable to start processing on a separate thread, and future that stores true / false depending on success
     */
    public static <T, X extends Throwable> Tuple3<CompletableFuture<Subscription>, Runnable, CompletableFuture<Boolean>> forEachEvent(
            final Stream<T> stream, final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError,
            final Runnable onComplete) {
        final CompletableFuture<Subscription> subscription = new CompletableFuture<>();
        final CompletableFuture<Boolean> streamCompleted = new CompletableFuture<>();
        return tuple(subscription, () -> {
            final Iterator<T> it = stream.iterator();
            final Object UNSET = new Object();
            Streams.stream(new Iterator<T>() {
                boolean errored = false;

                @Override
                public boolean hasNext() {
                    boolean result = false;
                    try {
                        result = it.hasNext();

                        return result;
                    } catch (final Throwable t) {

                        consumerError.accept(t);

                        errored = true;
                        return true;
                    } finally {
                        if (!result && !errored) {
                            try {
                                onComplete.run();
                            } finally {
                                streamCompleted.complete(true);
                            }
                        }
                    }
                }

                @Override
                public T next() {
                    try {
                        if (errored)
                            return (T) UNSET;
                        else
                            return it.next();
                    } finally {
                        errored = false;
                    }
                }

            })
                       .filter(t -> t != UNSET)
                       .forEach(consumerElement);
        } , streamCompleted);
    }
}
