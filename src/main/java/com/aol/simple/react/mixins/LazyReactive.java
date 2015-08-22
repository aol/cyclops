package com.aol.simple.react.mixins;

import java.util.Optional;
import java.util.function.Function;

import com.aol.simple.react.async.Adapter;
import com.aol.simple.react.async.pipes.Pipes;
import com.aol.simple.react.async.pipes.LazyReactors;
import com.aol.simple.react.stream.lazy.LazyReact;
import com.aol.simple.react.stream.traits.LazyFutureStream;
import com.aol.simple.react.threads.ParallelElasticPools;
import com.aol.simple.react.threads.SequentialElasticPools;


/**
 * Mixin / Trait for Reactive behaviour via simple-react
 * 
 * @author johnmcclean
 *
 */
public interface LazyReactive {
	/**
	 * Add a value to an simple-react Async.Adapter (Queue / Topic /Signal) if present
	 * Returns the Adapter wrapped in an Optional
	 * 
	 * @see Pipes#register(Object, com.aol.simple.react.async.Adapter)
	 * 
	 * @param key : identifier for registered Queue
	 * @param value : value to add to Queue
	 */
	default <K,V> Optional<Adapter<V>> enqueue(K key,V value){
		Optional<Adapter<V>> queue = Pipes.get(key);
		queue.map(adapter -> adapter.offer(value));
	
		return queue;
		
		
	}
	
	
	/**
	 * 
	 * Generate a sequentially executing single-threaded a LazyFutureStream that executes all tasks directly without involving
	 * a task executor between each stage (unless async operator invoked). A preconfigured LazyReact builder that will be supplied as
	 * input to the function supplied. The user Function should create a LazyFutureStream with any
	 * business logic stages predefined. This method will handle elastic scaling and pooling of Executor
	 * services. User code should call a terminal op on the returned LazyFutureStream
	 * @see Reactive#run(com.aol.simple.react.stream.traits.LazyFutureStream)
	 * 
	 * @param react Function that generates a LazyFutureStream from a LazyReact builder
	 * @return Generated LazyFutureStream
	 */
	default <T> LazyFutureStream<T> sync(Function<LazyReact,LazyFutureStream<T>> react){
		 LazyReact r  =SequentialElasticPools.lazyReact.nextReactor().withAsync(false);
		 return react.apply( r)
				 	.onFail(e->{ SequentialElasticPools.lazyReact.populate(r); throw e;})
				 	.peek(i->SequentialElasticPools.lazyReact.populate(r));
		 				 	
	}
	
	/**
	 * Switch LazyFutureStream into execution mode suitable for IO (reuse ioReactors task executor)
	 * 
	 * @param stream to convert to IO mode
	 * @return LazyFutureStream in IO mode
	 */
	default <T> LazyFutureStream<T> switchToIO(LazyFutureStream<T> stream){
		LazyReact react = LazyReactors.ioReact;
		return stream.withTaskExecutor(react.getExecutor()).withRetrier(react.getRetrier());
	}
	/**
	 *  Switch LazyFutureStream into execution mode suitable for CPU bound execution (reuse cpuReactors task executor)
	 * 
	 * @param stream to convert to CPU bound mode
	 * @return LazyFutureStream in CPU bound mode
	 */
	default <T> LazyFutureStream<T> switchToCPU(LazyFutureStream<T> stream){
		LazyReact react = LazyReactors.cpuReact;
		return stream.withTaskExecutor(react.getExecutor()).withRetrier(react.getRetrier());
	}
	/**
	 * @return Stream builder for IO Bound Streams
	 */
	default OptimizedLazyReact ioStream(){
		return new OptimizedLazyReact(LazyReactors.ioReact);
	}
	/**
	 * @return  Stream builder for CPU Bound Streams
	 */
	default OptimizedLazyReact cpuStream(){
		return new OptimizedLazyReact(LazyReactors.cpuReact);
	}
	/**
	 * Generate a multi-threaded LazyFutureStream that executes all tasks via 
	 *  a task executor between each stage (unless sync operator invoked). 
	 * A preconfigured LazyReact builder that will be supplied as
	 * input to the function supplied. The user Function should create a LazyFutureStream with any
	 * business logic stages predefined. This method will handle elastic scaling and pooling of Executor
	 * services. User code should call a terminal op on the returned LazyFutureStream
	 * @see #run(com.aol.simple.react.stream.traits.LazyFutureStream)
	 * 
	 * @param react Function that generates a LazyFutureStream from a LazyReact builder
	 * @return Generated LazyFutureStream
	 */
	default <T> LazyFutureStream<T>  async(Function<LazyReact,LazyFutureStream<T>> react){
		 LazyReact r  =ParallelElasticPools.lazyReact.nextReactor().withAsync(true);
		return  react.apply( r)
					.onFail(e->{ SequentialElasticPools.lazyReact.populate(r); throw e;})
					.peek(i->SequentialElasticPools.lazyReact.populate(r));
		 	
	}
	
	
	/**
	 * Convenience method that runs a LazyFutureStream without blocking the current thread
	 * @param stream to execute
	 */
	default <T> void run(LazyFutureStream<T> stream){
		stream.run();
	}
}