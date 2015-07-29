package com.aol.simple.react.reactivestreams;

import java.util.stream.Stream;

import lombok.Value;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.aol.simple.react.stream.traits.LazyFutureStream;

/**
 * 
 * ReactiveStreams publisher for standard Java 8 Stream implementations including
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
	
	/**
	 * This creates a synchronous publisher that publishes on the calling thread.
	 * 
	 * @param stream JDK Stream to turn into a Reactive Streams Publisher
	 * @return Reactive Streams Publisher
	 */
	public static <T> JDKReactiveStreamsPublisher<T> ofSync(Stream<T> stream){
		return new JDKReactiveStreamsPublisher<T>(true,stream);
	}
	/**
	 * This creates an asynchronous publisher that publishes on an external thread
	 * 
	 * @param stream JDK Stream to turn into a Reactive Streams Publisher
	 * @return Reactive Streams Publisher
	 */
	public static <T> JDKReactiveStreamsPublisher<T> ofAsync(Stream<T> stream){
		return new JDKReactiveStreamsPublisher<T>(false,stream);
	}
	/* 
	 *	@param s Reactive Streams subscriber
	 * @see org.reactivestreams.Publisher#subscribe(org.reactivestreams.Subscriber)
	 */
	@Override
	public void subscribe(Subscriber<? super T> s) {
		if(synchronous)
			LazyFutureStream.of(wrappedStream).sync().subscribe(s);
		else
			LazyFutureStream.of(wrappedStream).async().subscribe(s);
			
	}

	
}
