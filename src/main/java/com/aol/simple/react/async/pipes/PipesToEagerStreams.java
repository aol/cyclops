package com.aol.simple.react.async.pipes;

import static com.aol.simple.react.async.pipes.Pipes.registered;

import java.util.stream.Stream;

import com.aol.simple.react.async.Adapter;
import com.aol.simple.react.stream.traits.EagerFutureStream;

public class PipesToEagerStreams {
	/**
	 * Register a Queue, and get back a listening EagerFutureStream optimized for CPU Bound operations
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
	 * @param key : Adapter identifier
	 * @param adapter
	 * @return EagerFutureStream from supplied Queue, optimisied for CPU bound operation
	 */
	public static <V> EagerFutureStream<V> registerForCPU(Object key, Adapter<V> adapter){
		registered.put(key, adapter);
		return EagerReactors.cpuReact.from(adapter.stream());
	}
	/**
	 * Register a Queue, and get back a listening EagerFutureStream optimized for IO Bound operations
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
	 * @param key : Adapter identifier
	 * @param adapter
	 * @return EagerFutureStream from supplied Queue
	 */
	public static <V> EagerFutureStream<V> registerForIO(Object key, Adapter<V> adapter){
		registered.put(key, adapter);
		return EagerReactors.ioReact.from(adapter.stream());
	}
	/**
	 * @param key : Queue identifier
	 * @return EagerFutureStream that reads from specified Queue
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <V> EagerFutureStream<V> stream(Object key){
		return EagerFutureStream.eagerFutureStream(Stream.of()).fromStream( ((Adapter)registered.get(key)).stream());
	}
	/**
	 * @param key : Queue identifier
	 * @return EagerFutureStream that reads from specified Queue
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <V> EagerFutureStream<V> streamIOBound(Object key){
		return EagerReactors.ioReact.from(((Adapter)registered.get(key)).stream());
	}
	/**
	 * @param key : Queue identifier
	 * @return EagerFutureStream that reads from specified Queue
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <V> EagerFutureStream<V> streamCPUBound(Object key){
		return EagerReactors.cpuReact.from(((Adapter)registered.get(key)).stream());
	}
}
