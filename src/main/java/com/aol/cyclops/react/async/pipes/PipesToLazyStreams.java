package com.aol.cyclops.react.async.pipes;

import static com.aol.cyclops.react.async.pipes.Pipes.registered;

import com.aol.cyclops.data.async.Adapter;
import com.aol.cyclops.react.async.subscription.Subscription;
import com.aol.cyclops.types.futurestream.LazyFutureStream;

public class PipesToLazyStreams {
	/**
	 * Register a Queue, and get back a listening LazyFutureStream optimized for CPU Bound operations
	 * 
	 * Convert the LazyFutureStream to async mode to fan out operations across threads, after the first fan out operation definition 
	 * it should be converted to sync mode
	 * 
	 *  <pre>
	 * {@code
	 * LazyFutureStream<String> stream = PipesToLazyStreams.registerForCPU("test", QueueFactories.
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
		Subscription sub = new Subscription();
		return LazyReactors.cpuReact.from(adapter.stream(sub))
				.withSubscription(sub);
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
		Subscription sub = new Subscription();
		return LazyReactors.ioReact.from(adapter.stream(sub))
				.withSubscription(sub);
	}
	/**
	 * @param key : Queue identifier
	 * @return LazyFutureStream that reads from specified Queue
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <V> LazyFutureStream<V> stream(Object key){
		Subscription sub = new Subscription();
		return LazyFutureStream.lazyFutureStream(((Adapter)registered.get(key)).stream(sub))
							.withSubscription(sub);
	}
	/**
	 * @param key : Queue identifier
	 * @return LazyFutureStream that reads from specified Queue
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <V> LazyFutureStream<V> streamIOBound(Object key){
		Subscription sub = new Subscription();
		return LazyReactors.ioReact.from(((Adapter)registered.get(key)).stream(sub))
							.withSubscription(sub);
	}
	/**
	 * @param key : Queue identifier
	 * @return LazyFutureStream that reads from specified Queue
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <V> LazyFutureStream<V> streamCPUBound(Object key){
		Subscription sub = new Subscription();
		return LazyReactors.cpuReact.from(((Adapter)registered.get(key)).stream(sub))
				.withSubscription(sub);
	}
}
