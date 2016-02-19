package com.aol.cyclops.util.stream.reactivestreams;

import static com.aol.cyclops.types.futurestream.LazyFutureStream.lazyFutureStream;

import java.util.concurrent.Executor;
import java.util.stream.Stream;

import lombok.Value;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.aol.cyclops.control.LazyReact;
import com.aol.cyclops.react.ThreadPools;

/**
 * NB This implementation uses the LazyFutureStream publishing mechanism, which is inefficient for Sequential Streams
 * Publisher in ReactiveSeq is more efficient (this code should be modified in the future to use also).
 * 
 * ReactiveStreams publisher for standard Java 8 Stream implementations including
 * 
 *
 * java.util.stream.Stream
 * jool.Seq
 * cyclops.SequenceM
 * 
 * This provides both Asynchronous (external thread) and Synchronous (calling thread) publishing
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
@Value
public class JDKReactiveStreamsPublisher<T> implements Publisher<T>{

	boolean synchronous;
	Stream<T> wrappedStream;
	Executor exec;
	/**
	 * This creates a synchronous publisher that publishes on the calling thread.
	 * 
	 * @param stream JDK Stream to turn into a Reactive Streams Publisher
	 * @return Reactive Streams Publisher
	 */
	public static <T> JDKReactiveStreamsPublisher<T> ofSync(Stream<T> stream){
		return new JDKReactiveStreamsPublisher<T>(true,stream,null);
	}
	/**
	 * This creates an asynchronous publisher that publishes on an external thread
	 * 
	 * @param stream JDK Stream to turn into a Reactive Streams Publisher
	 * @return Reactive Streams Publisher
	 */
	public static <T> JDKReactiveStreamsPublisher<T> ofAsync(Stream<T> stream,Executor exec){
		return new JDKReactiveStreamsPublisher<T>(false,stream,exec);
	}
	/* 
	 *	@param s Reactive Streams subscriber
	 * @see org.reactivestreams.Publisher#subscribe(org.reactivestreams.Subscriber)
	 */
	@Override
	public void subscribe(Subscriber<? super T> s) {
		if(synchronous)
			lazyFutureStream(wrappedStream).sync().subscribe(s);
		else{
			new LazyReact(ThreadPools.getCurrentThreadExecutor()).withPublisherExecutor(exec).from(wrappedStream).async().subscribe(s);
		}
			
			
	}

	
}
