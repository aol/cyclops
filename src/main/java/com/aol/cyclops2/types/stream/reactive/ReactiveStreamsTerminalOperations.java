package com.aol.cyclops2.types.stream.reactive;

import java.util.function.Consumer;

import org.reactivestreams.Subscription;

/**
 * Interface for reactive-streams based terminal operations, requires simple-react to be on the classpath.
 *
 * 
 * @author johnmcclean
 *
 * @param <T> Element data in the Stream being processed.
 */
public interface ReactiveStreamsTerminalOperations<T> {
    /**
     * Perform a forEach operation over the Stream, without closing it, consuming only the specified number of elements from
     * the Stream, at this time. More elements can be consumed later, by called request on the returned Subscription
     *
     * e.g.
     * <pre>
     * {@code
     *     Subscription next = Spouts.of(1,2,3,4)
     *          					    .subscribe(System.out::println);
     *
     *     next.request(2);
     *     Thread.sleep(500);
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
     *
     * @param numberOfElements To consume from the Stream at this time
     * @param consumer To accept incoming events from the Stream
     * @return Subscription so that further processing can be continued or cancelled.
     */
   default <X extends Throwable> Subscription subscribe(Consumer<? super T> consumer){
       return forEach(0,consumer);
   }
    /**
     * Perform a forEach operation over the Stream  without closing it,  capturing any elements and errors in the supplied consumers, but only consuming
     * the specified number of elements from the Stream, at this time. More elements can be consumed later, by called request on the returned Subscription
     * <pre>
     * {@code
     *     Subscription next = ReactiveSeq.of(()->1,()->2,()->throw new RuntimeException(),()->4)
     *                                  .map(Supplier::get)
     *          					    .subscribe(System.out::println, e->e.printStackTrace());
     *
     *      next.request(2);
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
     *
     * @param numberOfElements To consume from the Stream at this time
     * @param consumer To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     * @return Subscription so that further processing can be continued or cancelled.
     */
     default <X extends Throwable> Subscription subscribe(Consumer<? super T> consumer,
                                               Consumer<? super Throwable> consumerError){
         return forEach(0,consumer,consumerError);
     }

    /**
     * Perform a forEach operation over the Stream  without closing it,  capturing any elements and errors in the supplied consumers, but only consuming
     * the specified number of elements from the Stream, at this time. More elements can be consumed later, by called request on the returned Subscription,
     * when the entire Stream has been processed an onComplete event will be recieved.
     *
     * <pre>
     * {@code
     *     Subscription next = ReactiveSeq.of(()->1,()->2,()->throw new RuntimeException(),()->4)
     *                                  .map(Supplier::get)
     *          					    .subscribe(System.out::println, e->e.printStackTrace(),()->System.out.println("the take!"));
     *
     *      next.request(2);
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
     *     The take!
     * }
     * </pre>
     * @param numberOfElements To consume from the Stream at this time
     * @param consumer To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     * @param onComplete To run after an onComplete event
     * @return Subscription so that further processing can be continued or cancelled.
     */
    default <X extends Throwable> Subscription subscribe(Consumer<? super T> consumer, Consumer<? super Throwable> consumerError,
                                               Runnable onComplete){
        return forEach(0,consumer,consumerError,onComplete);
    }

    /**
     * Perform a forEach operation over the Stream, without closing it, consuming only the specified number of elements from
     * the Stream, at this time. More elements can be consumed later, by called request on the returned Subscription
     * 
     * e.g.
     * <pre>
     * {@code
     *     Subscription next = ReactiveSeq.of(1,2,3,4)
     *          					    .forEach(2,System.out::println);
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
     * 
     * @param numberOfElements To consume from the Stream at this time
     * @param consumer To accept incoming events from the Stream
     * @return Subscription so that further processing can be continued or cancelled.
     */
    <X extends Throwable> Subscription forEach(long numberOfElements, Consumer<? super T> consumer);

    /**
     * Perform a forEach operation over the Stream  without closing it,  capturing any elements and errors in the supplied consumers, but only consuming 
     * the specified number of elements from the Stream, at this time. More elements can be consumed later, by called request on the returned Subscription 
     * <pre>
     * {@code
     *     Subscription next = ReactiveSeq.of(()->1,()->2,()->throw new RuntimeException(),()->4)
     *                                  .map(Supplier::get)
     *          					    .forEach(2,System.out::println, e->e.printStackTrace());
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
     * 
     * @param numberOfElements To consume from the Stream at this time
     * @param consumer To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     * @return Subscription so that further processing can be continued or cancelled.
     */
    <X extends Throwable> Subscription forEach(long numberOfElements, Consumer<? super T> consumer,
                                               Consumer<? super Throwable> consumerError);

    /**
     * Perform a forEach operation over the Stream  without closing it,  capturing any elements and errors in the supplied consumers, but only consuming 
     * the specified number of elements from the Stream, at this time. More elements can be consumed later, by called request on the returned Subscription,
     * when the entire Stream has been processed an onComplete event will be recieved.
     * 
     * <pre>
     * {@code
     *     Subscription next = ReactiveSeq.of(()->1,()->2,()->throw new RuntimeException(),()->4)
     *                                  .map(Supplier::get)
     *          					    .forEach(2,System.out::println, e->e.printStackTrace(),()->System.out.println("the take!"));
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
     *     The take!
     * }
     * </pre>	 
     * @param numberOfElements To consume from the Stream at this time
     * @param consumer To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     * @param onComplete To run after an onComplete event
     * @return Subscription so that further processing can be continued or cancelled.
     */
    <X extends Throwable> Subscription forEach(long numberOfElements, Consumer<? super T> consumer, Consumer<? super Throwable> consumerError,
                                               Runnable onComplete);

    /**
     *  Perform a forEach operation over the Stream    capturing any elements and errors in the supplied consumers,  
     * <pre>
     * {@code
     *     Subscription next = ReactiveSeq.of(()->1,()->2,()->throw new RuntimeException(),()->4)
     *                                  .map(Supplier::get)
     *          					    .forEach(System.out::println, e->e.printStackTrace());
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
     * @param consumerElement To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     * 
     */
    <X extends Throwable> void forEach(Consumer<? super T> consumerElement, Consumer<? super Throwable> consumerError);

    /**
     * Perform a forEach operation over the Stream  capturing any elements and errors in the supplied consumers
     * when the entire Stream has been processed an onComplete event will be recieved.
     * 
     * <pre>
     * {@code
     *     Subscription next = ReactiveSeq.of(()->1,()->2,()->throw new RuntimeException(),()->4)
     *                                  .map(Supplier::get)
     *          					    .forEachEvents(System.out::println, e->e.printStackTrace(),()->System.out.println("the take!"));
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
     * @param consumerElement To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     * @param onComplete To run after an onComplete event
     * 
     */
    <X extends Throwable> void forEach(Consumer<? super T> consumerElement, Consumer<? super Throwable> consumerError, Runnable onComplete);

}
