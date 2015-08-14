package com.aol.simple.react.mixins;

import java.util.Optional;
import java.util.function.Function;

import com.aol.simple.react.async.Adapter;
import com.aol.simple.react.async.pipes.EagerReactors;
import com.aol.simple.react.async.pipes.LazyReactors;
import com.aol.simple.react.async.pipes.Pipes;
import com.aol.simple.react.stream.eager.EagerReact;
import com.aol.simple.react.stream.traits.EagerFutureStream;
import com.aol.simple.react.threads.ParallelElasticPools;
import com.aol.simple.react.threads.SequentialElasticPools;


/**
 * Mixin / Trait for Reactive behaviour via simple-react
 * 
 * @author johnmcclean
 *
 */
public interface EagerReactive {
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
	 * Generate a sequentially executing single-threaded a EagerFutureStream that executes all tasks directly without involving
	 * a task executor between each stage (unless async operator invoked). A preconfigured EagerReact builder that will be supplied as
	 * input to the function supplied. The user Function should create a EagerFutureStream with any
	 * business logic stages predefined. This method will handle elastic scaling and pooling of Executor
	 * services. User code should call a terminal op on the returned EagerFutureStream
	 * @see Reactive#run(com.aol.simple.react.stream.traits.EagerFutureStream)
	 * 
	 * @param react Function that generates a EagerFutureStream from a EagerReact builder
	 * @return Generated EagerFutureStream
	 */
	default <T> EagerFutureStream<T> sync(Function<EagerReact,EagerFutureStream<T>> react){
		 EagerReact r  =SequentialElasticPools.eagerReact.nextReactor().withAsync(false);
		 return react.apply( r)
				 	.onFail(e->{ SequentialElasticPools.eagerReact.populate(r); throw e;})
				 	.peek(i->SequentialElasticPools.eagerReact.populate(r));
		 				 	
	}
	
	/**
	 * Switch EagerFutureStream into execution mode suitable for IO (reuse ioReactors task executor)
	 * 
	 * @param stream to convert to IO mode
	 * @return EagerFutureStream in IO mode
	 */
	default <T> EagerFutureStream<T> switchToIO(EagerFutureStream<T> stream){
		EagerReact react = EagerReactors.ioReact;
		return stream.withTaskExecutor(react.getExecutor()).withRetrier(react.getRetrier());
	}
	/**
	 *  Switch EagerFutureStream into execution mode suitable for CPU bound execution (reuse cpuReactors task executor)
	 * 
	 * @param stream to convert to CPU bound mode
	 * @return EagerFutureStream in CPU bound mode
	 */
	default <T> EagerFutureStream<T> switchToCPU(EagerFutureStream<T> stream){
		EagerReact react = EagerReactors.cpuReact;
		return stream.withTaskExecutor(react.getExecutor()).withRetrier(react.getRetrier());
	}
	/**
	 * @return Stream builder for IO Bound Streams
	 */
	default OptimizedEagerReact ioStream(){
		return new OptimizedEagerReact(EagerReactors.ioReact);
	}
	/**
	 * @return  Stream builder for CPU Bound Streams
	 */
	default OptimizedEagerReact cpuStream(){
		return new OptimizedEagerReact(EagerReactors.cpuReact);
	}
	/**
	 * Generate a multi-threaded EagerFutureStream that executes all tasks via 
	 *  a task executor between each stage (unless sync operator invoked). 
	 * A preconfigured EagerReact builder that will be supplied as
	 * input to the function supplied. The user Function should create a EagerFutureStream with any
	 * business logic stages predefined. This method will handle elastic scaling and pooling of Executor
	 * services. User code should call a terminal op on the returned EagerFutureStream
	 * @see Reactive#run(com.aol.simple.react.stream.traits.EagerFutureStream)
	 * 
	 * @param react Function that generates a EagerFutureStream from a EagerReact builder
	 * @return Generated EagerFutureStream
	 */
	default <T> EagerFutureStream<T>  async(Function<EagerReact,EagerFutureStream<T>> react){
		 EagerReact r  =ParallelElasticPools.eagerReact.nextReactor().withAsync(true);
		return  react.apply( r)
					.onFail(e->{ SequentialElasticPools.eagerReact.populate(r); throw e;})
					.peek(i->SequentialElasticPools.eagerReact.populate(r));
		 	
	}
	
	
	
}