package com.aol.cyclops.react.async.pipes;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.aol.cyclops.data.async.Adapter;
import com.aol.cyclops.react.threads.SequentialElasticPools;
import com.aol.cyclops.types.futurestream.LazyFutureStream;
import com.aol.cyclops.types.stream.reactive.SeqSubscriber;

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
		LazyFutureStream<String> stream =  PipesToLazyStreams.cpuBoundStream("test");
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
	
	/**
	 * @param key for registered simple-react async.Adapter
	 * @return A Reactive Streams Publisher Registered for the given key
	 */
	public static<T> Optional<Publisher<T>> publisher(Object key, Executor publishWith){
		if(!registered.containsKey(key))
			return Optional.empty();
		return Optional.of(LazyFutureStream.lazyFutureStream(((Adapter)registered.get(key)).stream()).async()
				.withPublisherExecutor(publishWith));
	}
	/**
	 * @param key for registered simple-react async.Adapter
	 * @return A Reactive Streams Publisher Registered for the given key
	 */
	public static<T> Optional<Publisher<T>> publisher(Object key){
		if(!registered.containsKey(key))
			return Optional.empty();
		return Optional.of(LazyFutureStream.lazyFutureStream(((Adapter)registered.get(key)).stream()).sync());
	}
	/**
	 * @param key for registered simple-react async.Adapter
	 * @param subscriber Reactive Streams subscriber for data on this pipe
	 */
	public static<T> void subscribeTo(Object key,Subscriber<T> subscriber,Executor subscribeOn){
		LazyFutureStream.lazyFutureStream(((Adapter)registered.get(key)).stream())
						.async().withPublisherExecutor(subscribeOn).subscribe(subscriber);
	}
	/**
	 * @param key for registered simple-react async.Adapter
	 * @param publisher Reactive Streams publisher  to push data onto this pipe
	 */
	public static<T> void publishTo(Object key,Publisher<T> publisher){
		SeqSubscriber<T> sub = SeqSubscriber.subscriber();
		publisher.subscribe(sub);
		registered.get(key).fromStream((Stream)sub.stream());
	}
	/**
	 * @param key for registered simple-react async.Adapter
	 * @param publisher Reactive Streams publisher  to push data onto this pipe
	 */
	public static<T> void publishToAsync(Object key,Publisher<T> publisher){
		SequentialElasticPools.simpleReact.react(er->er.of(publisher)
								.peek(p->publishTo(key,p)));
	}
	
}
