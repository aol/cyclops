package com.aol.simple.react.async.pipes;

import static com.aol.simple.react.async.pipes.Pipes.registered;
import static com.aol.simple.react.stream.traits.LazyFutureStream.lazyFutureStream;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.aol.simple.react.async.Adapter;
import com.aol.simple.react.reactivestreams.JDKReactiveStreamsSubscriber;
import com.aol.simple.react.stream.traits.LazyFutureStream;
import com.aol.simple.react.threads.SequentialElasticPools;

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
		JDKReactiveStreamsSubscriber<T> sub = new JDKReactiveStreamsSubscriber<>();
		publisher.subscribe(sub);
		registered.get(key).fromStream((Stream)sub.getStream());
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
