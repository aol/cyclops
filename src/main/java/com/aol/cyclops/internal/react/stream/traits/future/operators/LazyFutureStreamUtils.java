package com.aol.cyclops.internal.react.stream.traits.future.operators;

import static org.jooq.lambda.tuple.Tuple.tuple;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.jooq.lambda.tuple.Tuple3;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.aol.cyclops.types.futurestream.LazyFutureStream;

public class LazyFutureStreamUtils {

    /**
     * Perform a forEach operation over the Stream, without closing it, consuming only the specified number of elements from
     * the Stream, at this time. More elements can be consumed later, by called request on the returned Subscription
     * 
     * <pre>
     * @{code
     *     Subscription next = StreamUtils.forEachX(Stream.of(1,2,3,4),2,System.out::println);
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
     * @param Stream - the Stream to consume data from
     * @param numberOfElements To consume from the Stream at this time
     * @param consumer To accept incoming events from the Stream
     * @return Subscription so that further processing can be continued or cancelled.
     */
    public static <T, X extends Throwable> Tuple3<CompletableFuture<Subscription>, Runnable, CompletableFuture<Boolean>> forEachX(
            LazyFutureStream<T> stream, long x, Consumer<? super T> consumerElement) {
        CompletableFuture<Subscription> subscription = new CompletableFuture<>();
        CompletableFuture<Boolean> streamCompleted = new CompletableFuture<>();
        return tuple(subscription, () -> {
            stream.subscribe(new Subscriber<T>() {

                @Override
                public void onSubscribe(Subscription s) {
                    Objects.requireNonNull(s);
                    s.request(x);
                    subscription.complete(s);
                }

                @Override
                public void onNext(T t) {
                    consumerElement.accept(t);

                }

                @Override
                public void onError(Throwable t) {

                }

                @Override
                public void onComplete() {
                    streamCompleted.complete(true);

                }

            });
        } , streamCompleted);
    }

    /**
     * Perform a forEach operation over the Stream  without closing it,  capturing any elements and errors in the supplied consumers, but only consuming 
     * the specified number of elements from the Stream, at this time. More elements can be consumed later, by called request on the returned Subscription 
     * <pre>
     * @{code
     *     Subscription next = StreamUtils.forEachXWithError(Stream.of(()->1,()->2,()->{throw new RuntimeException()},()->4)
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
     * @param Stream - the Stream to consume data from
     * @param numberOfElements To consume from the Stream at this time
     * @param consumer To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     * @param onComplete To run after an onComplete event
     * @return Subscription so that further processing can be continued or cancelled.
     */
    public static <T, X extends Throwable> Tuple3<CompletableFuture<Subscription>, Runnable, CompletableFuture<Boolean>> forEachXWithError(
            LazyFutureStream<T> stream, long x, Consumer<? super T> consumerElement, Consumer<? super Throwable> consumerError) {
        CompletableFuture<Subscription> subscription = new CompletableFuture<>();
        CompletableFuture<Boolean> streamCompleted = new CompletableFuture<>();
        return tuple(subscription, () -> {
            stream.subscribe(new Subscriber<T>() {

                @Override
                public void onSubscribe(Subscription s) {
                    Objects.requireNonNull(s);
                    s.request(x);
                    subscription.complete(s);
                }

                @Override
                public void onNext(T t) {
                    consumerElement.accept(t);

                }

                @Override
                public void onError(Throwable t) {
                    consumerError.accept(t);
                }

                @Override
                public void onComplete() {
                    streamCompleted.complete(true);

                }

            });
        } , streamCompleted);
    }

    /**
     * Perform a forEach operation over the Stream  without closing it,  capturing any elements and errors in the supplied consumers, but only consuming 
     * the specified number of elements from the Stream, at this time. More elements can be consumed later, by called request on the returned Subscription,
     * when the entire Stream has been processed an onComplete event will be recieved.
     * 
     * <pre>
     * @{code
     *     Subscription next = StreamUtils.forEachXEvents(Stream.of(()->1,()->2,()->{throw new RuntimeException()},()->4)
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
     * @param Stream - the Stream to consume data from	 
     * @param numberOfElements To consume from the Stream at this time
     * @param consumer To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     * @param onComplete To run after an onComplete event
     * @return Subscription so that further processing can be continued or cancelled.
     */
    public static <T, X extends Throwable> Tuple3<CompletableFuture<Subscription>, Runnable, CompletableFuture<Boolean>> forEachXEvents(
            LazyFutureStream<T> stream, long x, Consumer<? super T> consumerElement, Consumer<? super Throwable> consumerError, Runnable onComplete) {
        CompletableFuture<Subscription> subscription = new CompletableFuture<>();
        CompletableFuture<Boolean> streamCompleted = new CompletableFuture<>();
        return tuple(subscription, () -> {
            stream.subscribe(new Subscriber<T>() {

                @Override
                public void onSubscribe(Subscription s) {
                    Objects.requireNonNull(s);
                    s.request(x);
                    subscription.complete(s);
                }

                @Override
                public void onNext(T t) {
                    consumerElement.accept(t);

                }

                @Override
                public void onError(Throwable t) {
                    consumerError.accept(t);
                }

                @Override
                public void onComplete() {
                    streamCompleted.complete(true);
                    onComplete.run();
                }

            });
        } , streamCompleted);
    }

    /**
     *  Perform a forEach operation over the Stream    capturing any elements and errors in the supplied consumers,  
     * <pre>
     * @{code
     *     Subscription next = StreanUtils.forEachWithError(Stream.of(()->1,()->2,()->{throw new RuntimeException()},()->4)
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
     * @param Stream - the Stream to consume data from	 
     * @param consumer To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     */
    public static <T, X extends Throwable> Tuple3<CompletableFuture<Subscription>, Runnable, CompletableFuture<Boolean>> forEachWithError(
            LazyFutureStream<T> stream, Consumer<? super T> consumerElement, Consumer<? super Throwable> consumerError) {
        CompletableFuture<Subscription> subscription = new CompletableFuture<>();
        CompletableFuture<Boolean> streamCompleted = new CompletableFuture<>();
        return tuple(subscription, () -> {
            stream.subscribe(new Subscriber<T>() {

                @Override
                public void onSubscribe(Subscription s) {
                    Objects.requireNonNull(s);
                    subscription.complete(s);
                    s.request(Long.MAX_VALUE);

                }

                @Override
                public void onNext(T t) {
                    consumerElement.accept(t);

                }

                @Override
                public void onError(Throwable t) {
                    consumerError.accept(t);

                }

                @Override
                public void onComplete() {
                    streamCompleted.complete(true);

                }

            });
        } , streamCompleted);

    }

    /**
     * Perform a forEach operation over the Stream  capturing any elements and errors in the supplied consumers
     * when the entire Stream has been processed an onComplete event will be recieved.
     * 
     * <pre>
     * @{code
     *     Subscription next = StreamUtils.forEachEvents(Stream.of(()->1,()->2,()->{throw new RuntimeException()},()->4)
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
     * @param Stream - the Stream to consume data from	
     * @param consumer To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     * @param onComplete To run after an onComplete event
     * @return Subscription so that further processing can be continued or cancelled.
     */
    public static <T, X extends Throwable> Tuple3<CompletableFuture<Subscription>, Runnable, CompletableFuture<Boolean>> forEachEvent(
            LazyFutureStream<T> stream, Consumer<? super T> consumerElement, Consumer<? super Throwable> consumerError, Runnable onComplete) {
        CompletableFuture<Subscription> subscription = new CompletableFuture<>();
        CompletableFuture<Boolean> streamCompleted = new CompletableFuture<>();
        return tuple(subscription, () -> {
            stream.subscribe(new Subscriber<T>() {

                @Override
                public void onSubscribe(Subscription s) {
                    Objects.requireNonNull(s);
                    subscription.complete(s);
                    s.request(Long.MAX_VALUE);

                }

                @Override
                public void onNext(T t) {
                    consumerElement.accept(t);

                }

                @Override
                public void onError(Throwable t) {
                    consumerError.accept(t);

                }

                @Override
                public void onComplete() {
                    streamCompleted.complete(true);
                    onComplete.run();

                }

            });
        } , streamCompleted);
    }
}
