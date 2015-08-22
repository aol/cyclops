package com.aol.simple.react.async.pipes;

import static com.aol.simple.react.async.pipes.Pipes.registered;

import java.util.stream.Stream;

import com.aol.simple.react.async.Adapter;
import com.aol.simple.react.stream.ThreadPools;
import com.aol.simple.react.stream.eager.EagerReact;
import com.aol.simple.react.stream.traits.EagerFutureStream;
import com.aol.simple.react.threads.SequentialElasticPools;
import com.nurkiewicz.asyncretry.AsyncRetryExecutor;

public class PipesToEagerStreams {
	/**
	 * Pass in a Queue, and get back a listening EagerFutureStream optimized for CPU Bound operations
	 * 
	 * Convert the EagerFutureStream to async mode to fan out operations across threads, after the first fan out operation definition 
	 * it should be converted to sync mode
	 * 
	 *  <pre>
	 * {@code
	 * EagerFutureStream<String> stream = Pipes.registerForCPU("test", QueueFactories.
											<String>boundedNonBlockingQueue(100)
												.build());
		stream.filter(it->it!=null)
		      .async()
		      .peek(this::process)
		      .sync()
		      .forEach(System.out::println);
	 * 
	 * }</pre>
	 *
	 * @param adapter
	 * @return EagerFutureStream from supplied Queue, optimisied for CPU bound operation
	 */
	public static <V> EagerFutureStream<V> streamCPUBound(Adapter<V> adapter){
		
		return EagerReactors.cpuReact.fromStreamAsync(adapter.streamCompletableFutures());
	}
	/**
	 * Pass in a Queue, and get back a listening EagerFutureStream optimized for IO Bound operations
	 * 
	 * <pre>
	 * {@code
	 * EagerFutureStream<String> stream = Pipes.registerForIO("test", QueueFactories.
											<String>boundedNonBlockingQueue(100)
												.build());
		stream.filter(it->it!=null)
		      .async()
		      .peek(this::load)
		      .sync()
		      .run(System.out::println);
	 * 
	 * }</pre>
	 * 
	 * 
	 * @param adapter
	 * @return EagerFutureStream from supplied Queue
	 */
	public static <V> EagerFutureStream<V> streamIOBound(Adapter<V> adapter){
		
		return EagerReactors.ioReact.fromStreamAsync(adapter.streamCompletableFutures());
	}
	/**
	 * 
	 * @return EagerFutureStream that reads from specified Queue
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <V> EagerFutureStream<V> stream(Adapter<V> adapter){
		return new EagerReact(ThreadPools.getSequential(),new AsyncRetryExecutor(ThreadPools.getSequentialRetry()),false).fromStreamAsync(adapter.streamCompletableFutures());
	}
	
	
}
