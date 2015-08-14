package com.aol.simple.react.async.pipes;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.aol.simple.react.async.Adapter;
import com.aol.simple.react.stream.traits.LazyFutureStream;

/**
 * Store for Pipes for cross-thread communication
 * 
 * @author johnmcclean
 *
 */
public class Pipes {
	
	static final ConcurrentMap<Object,Adapter<?>> registered = new ConcurrentHashMap<>();
	
	
	/**
	 * @param key : Adapter identifier
	 * @return selected Queue
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <K,V> Optional<Adapter<V>> get(K key){
		return Optional.ofNullable((Adapter)registered.get(key));
	}
	/**
	 * Register a Queue, and get back a listening LazyFutureStream that runs on a single thread
	 * (not the calling thread)
	 * 
	 * <pre>
	 * {@code
	 * Pipes.register("test", QueueFactories.
											<String>boundedNonBlockingQueue(100)
												.build());
		LazyFutureStream<String> stream =  Pipes.cpuBoundStream("test");
		stream.filter(it->it!=null).peek(System.out::println).run();
	 * 
	 * }</pre>
	 * 
	 * @param key : Adapter identifier
	 * @param adapter
	 * 
	 */
	public static <V> void register(Object key, Adapter<V> adapter){
		registered.put(key, adapter);
		
	}
	
	

	public static void clear() {
		 registered.clear();
		
	}
	
}
