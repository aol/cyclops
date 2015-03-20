package com.aol.simple.react.stream;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.aol.simple.react.async.Subscription;
import com.aol.simple.react.stream.traits.SimpleReactStream;

public abstract class BaseLazySimpleReact extends BaseSimpleReact{
	/**
	 * Generate an infinite reactive flow. Requires a lazy flow.
	 * 
	 * The flow will run indefinitely unless / until the provided Supplier throws an Exception
	 * 
	 * @see com.aol.simple.react.async.Queue   SimpleReact Queue for a way to create a more managable infinit flow
	 * 
	 * @param s Supplier to generate the infinite flow
	 * @return Next stage in the flow
	 */
	public <U> SimpleReactStream< U> reactInfinitely(final Supplier<U> s) {
		if(isEager())
			throw new InfiniteProcessingException("To reactInfinitely use a lazy stream");
		Subscription sub = new Subscription();
		SimpleReactStream stream = construct(StreamSupport.stream(
                new InfiniteClosingSpliterator(Long.MAX_VALUE, () -> CompletableFuture.completedFuture(s.get()),sub), false),
				this.getExecutor(),getRetrier(),false).withSubscription(sub);
		
		return stream;
		

	}
	
	public <U> SimpleReactStream<U> iterateInfinitely(final U seed, final UnaryOperator<U> f){
		if(isEager())
			throw new InfiniteProcessingException("To iterateInfinitely use a lazy stream");
		return construct(Stream.iterate(seed, it -> f.apply(it)).map(it -> CompletableFuture.completedFuture(it)),
				this.getExecutor(),getRetrier(),false);
	}
}
