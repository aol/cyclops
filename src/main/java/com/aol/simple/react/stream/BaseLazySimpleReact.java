package com.aol.simple.react.stream;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.StreamSupport;

import lombok.AllArgsConstructor;

import com.aol.simple.react.async.Subscription;
import com.aol.simple.react.stream.traits.SimpleReactStream;

public abstract class BaseLazySimpleReact extends BaseSimpleReact{
	
	
	
	/**
	 * Generate an infinite reactive flow. Requires a lazy flow. Supplier will be executed multiple times sequentially / synchronously by populating thread.
	 * 
	 * 
	 * The flow will run indefinitely unless / until the provided Supplier throws an Exception
	 * 
	 * @see com.aol.simple.react.async.Queue   SimpleReact Queue for a way to create a more managable infinit flow
	 * 
	 * @param s Supplier to generate the infinite flow
	 * @return Next stage in the flow
	 */
	public <U> SimpleReactStream< U> reactInfinitely(final Supplier<U> s) {
		
		Subscription sub = new Subscription();
		SimpleReactStream stream = construct(StreamSupport.stream(
                new InfiniteClosingSpliterator(Long.MAX_VALUE, () -> CompletableFuture.completedFuture(s.get()),sub), false),
				null).withSubscription(sub);
		
		return stream;
		

	}
	/**
	 * Generate an infinite reactive flow. Requires a lazy flow. Supplier may be executed multiple times in parallel asynchronously by populating thread.
	 * Active CompletableFutures may grow rapidly.
	 * 
	 * The flow will run indefinitely unless / until the provided Supplier throws an Exception
	 * 
	 * @see com.aol.simple.react.async.Queue   SimpleReact Queue for a way to create a more managable infinit flow
	 * 
	 * @param s Supplier to generate the infinite flow
	 * @return Next stage in the flow
	 */
	public <U> SimpleReactStream< U> reactInfinitelyAsync(final Supplier<U> s) {
		
		Subscription sub = new Subscription();
		SimpleReactStream stream = construct(StreamSupport.stream(
                new InfiniteClosingSpliterator(Long.MAX_VALUE, () -> CompletableFuture.supplyAsync(s),sub), false),
				null).withSubscription(sub);
		
		return stream;
		

	}
	private static final Object NONE = new Object();
	/**
	 * Iterate infinitely using the supplied seed and function
	 * 
	 * @param seed Initial value
	 * @param f Function that performs the iteration
	 * @return Next stage in the flow / stream
	 */
	public <U> SimpleReactStream<U> iterateInfinitely(final U seed, final UnaryOperator<U> f){
		
		Subscription sub = new Subscription();
		 final Iterator<CompletableFuture<U>> iterator = new Iterator<CompletableFuture<U>> () {
	            @SuppressWarnings("unchecked")
	            CompletableFuture<U> t = CompletableFuture.completedFuture((U) NONE);

	            @Override
	            public boolean hasNext() {
	                return true;
	            }

	            @Override
	            public CompletableFuture<U>  next() {
	                return t = (t.join() == NONE) ? CompletableFuture.completedFuture(seed) : CompletableFuture.completedFuture(f.apply(t.join()));
	            }
	        };
	      return  construct(StreamSupport.stream(  new InfiniteClosingSpliteratorFromIterator(Long.MAX_VALUE,iterator,sub),false),
					null);

	}
}
