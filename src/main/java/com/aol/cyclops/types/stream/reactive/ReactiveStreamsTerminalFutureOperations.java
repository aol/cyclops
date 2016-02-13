package com.aol.cyclops.types.stream.reactive;

import java.util.function.Consumer;

/**
 * Interface for reactive-streams based terminal operations, requires simple-react to be on the classpath.
 * 
 * @author johnmcclean
 *
 * @param <T> Element data in the Stream being processed.
 */
public interface ReactiveStreamsTerminalFutureOperations<T> {

	/**
	 * Perform a forEach operation over the Stream, without closing it, consuming only the specified number of elements from
	 * the Stream, at this time. More elements can be consumed later, by called request on the returned Subscription
	 * 
	 * <pre>
	 * @{code
	 *     ReactiveTask next = SequenceM.of(1,2,3,4)
	 *                                  .futureOperations(exec)
	 *          					    .forEachX(2,System.out::println)
	 *          						.join();
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
	 * @return ReactiveTask so that further processing can be continued or cancelled.
	 */
	<X extends Throwable> ReactiveTask forEachX(long numberOfElements,Consumer<? super T> consumer);
	
	/**
	 * Perform a forEach operation over the Stream  without closing it,  capturing any elements and errors in the supplied consumers, but only consuming 
	 * the specified number of elements from the Stream, at this time. More elements can be consumed later, by called request on the returned Subscription 
	 * <pre>
	 * @{code
	 *     ReactiveTask next = SequenceM.of(()->1,()->2,()->{throw new RuntimeException()},()->4)
	 *                                  .futureOperations(exec)
	 *                                  .map(Supplier::get)
	 *          					    .forEachXWithError(2,System.out::println, e->e.printStackTrace());
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
	 * @param onComplete To run after an onComplete event
	 * @return ReactiveTask so that further processing can be continued or cancelled.
	 */
	<X extends Throwable> ReactiveTask forEachXWithError(long numberOfElements,Consumer<? super T> consumer,Consumer<? super Throwable> consumerError);
	/**
	 * Perform a forEach operation over the Stream  without closing it,  capturing any elements and errors in the supplied consumers, but only consuming 
	 * the specified number of elements from the Stream, at this time. More elements can be consumed later, by called request on the returned Subscription,
	 * when the entire Stream has been processed an onComplete event will be recieved.
	 * 
	 * <pre>
	 * @{code
	 *     ReactiveTask next = SequenceM.of(()->1,()->2,()->{throw new RuntimeException()},()->4)
	 *                                  .futureOperations(exec)
	 *                                  .map(Supplier::get)
	 *          					    .forEachXEvents(2,System.out::println, e->e.printStackTrace(),()->System.out.println("the end!"));
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
	 * @param numberOfElements To consume from the Stream at this time
	 * @param consumer To accept incoming elements from the Stream
	 * @param consumerError To accept incoming processing errors from the Stream
	 * @param onComplete To run after an onComplete event
	 * @return ReactiveTask so that further processing can be continued or cancelled.
	 */
	<X extends Throwable> ReactiveTask forEachXEvents(long numberOfElements,Consumer<? super T> consumer,Consumer<? super Throwable> consumerError, Runnable onComplete);
	
	/**
	 *  Perform a forEach operation over the Stream    capturing any elements and errors in the supplied consumers,  
	 * <pre>
	 * @{code
	 *     ReactiveTask next = SequenceM.of(()->1,()->2,()->{throw new RuntimeException()},()->4)
	 *                                  .futureOperations(exec)
	 *                                  .map(Supplier::get)
	 *          					    .forEachWithError(System.out::println, e->e.printStackTrace());
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
	 * @param consumer To accept incoming elements from the Stream
	 * @param consumerError To accept incoming processing errors from the Stream
	 * @return ReactiveTask so that further processing can be continued or cancelled.
	 */
	<X extends Throwable> ReactiveTask forEachWithError(Consumer<? super T> consumerElement,
			Consumer<? super Throwable> consumerError);
	
	/**
	 * Perform a forEach operation over the Stream  capturing any elements and errors in the supplied consumers
	 * when the entire Stream has been processed an onComplete event will be recieved.
	 * 
	 * <pre>
	 * @{code
	 *     ReactiveTask next = SequenceM.of(()->1,()->2,()->{throw new RuntimeException()},()->4)
	 *                                  .futureOperations(exec)
	 *                                  .map(Supplier::get)
	 *          					    .forEachEvents(System.out::println, e->e.printStackTrace(),()->System.out.println("the end!"));
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
	 * @param consumer To accept incoming elements from the Stream
	 * @param consumerError To accept incoming processing errors from the Stream
	 * @param onComplete To run after an onComplete event
	 * @return ReactiveTask so that further processing can be continued or cancelled.
	 */
	<X extends Throwable> ReactiveTask forEachEvent(Consumer<? super T> consumerElement,
			Consumer<? super Throwable> consumerError,
			Runnable onComplete);
	
	
}
