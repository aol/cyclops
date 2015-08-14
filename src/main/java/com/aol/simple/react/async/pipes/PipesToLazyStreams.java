package com.aol.simple.react.async.pipes;

import static com.aol.simple.react.async.pipes.Pipes.registered;

import com.aol.simple.react.async.Adapter;
import com.aol.simple.react.stream.traits.LazyFutureStream;

public class PipesToLazyStreams {
	/**
	 * Register a Queue, and get back a listening LazyFutureStream optimized for CPU Bound operations
	 * 
	 * Convert the LazyFutureStream to async mode to fan out operations across threads, after the first fan out operation definition 
	 * it should be converted to sync mode
	 * 
	 *  <pre>
	 * {@code
	 * LazyFutureStream<String> stream = Pipes.registerForCPU("test", QueueFactories.
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
	 * @return LazyFutureStream from supplied Queue, optimisied for CPU bound operation
	 */
	public static <V> LazyFutureStream<V> registerForCPU(Object key, Adapter<V> adapter){
		registered.put(key, adapter);
		return LazyReactors.cpuReact.from(adapter.stream());
	}
	/**
	 * Register a Queue, and get back a listening LazyFutureStream optimized for IO Bound operations
	 * 
	 * <pre>
	 * {@code
	 * LazyFutureStream<String> stream = Pipes.registerForIO("test", QueueFactories.
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
	 * @return LazyFutureStream from supplied Queue
	 */
	public static <V> LazyFutureStream<V> registerForIO(Object key, Adapter<V> adapter){
		registered.put(key, adapter);
		return LazyReactors.ioReact.from(adapter.stream());
	}
	/**
	 * @param key : Queue identifier
	 * @return LazyFutureStream that reads from specified Queue
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <V> LazyFutureStream<V> stream(Object key){
		return LazyFutureStream.lazyFutureStream(((Adapter)registered.get(key)).stream());
	}
	/**
	 * @param key : Queue identifier
	 * @return LazyFutureStream that reads from specified Queue
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <V> LazyFutureStream<V> streamIOBound(Object key){
		return LazyReactors.ioReact.from(((Adapter)registered.get(key)).stream());
	}
	/**
	 * @param key : Queue identifier
	 * @return LazyFutureStream that reads from specified Queue
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <V> LazyFutureStream<V> streamCPUBound(Object key){
		return LazyReactors.cpuReact.from(((Adapter)registered.get(key)).stream());
	}
}
