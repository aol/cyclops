package com.aol.cyclops.javaslang.reactivestreams;

import java.util.concurrent.Executor;
import java.util.stream.Stream;

import javaslang.collection.Traversable;
import lombok.Value;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.aol.cyclops.javaslang.ToStream;
import com.aol.simple.react.stream.ThreadPools;
import com.aol.simple.react.stream.lazy.LazyReact;

@Value
public class JavaslangReactiveStreamsPublisher<T> implements Publisher<T>{

		boolean synchronous;
		Traversable<T> wrappedStream;
		Executor exec;
		/**
		 * This creates a synchronous publisher that publishes on the calling thread.
		 * 
		 * @param stream JDK Stream to turn into a Reactive Streams Publisher
		 * @return Reactive Streams Publisher
		 */
		public static <T> JavaslangReactiveStreamsPublisher<T> ofSync(Traversable<T> stream){
			return new JavaslangReactiveStreamsPublisher<T>(true,stream,null);
		}
		/**
		 * This creates an asynchronous publisher that publishes on an external thread
		 * 
		 * @param stream JDK Stream to turn into a Reactive Streams Publisher
		 * @return Reactive Streams Publisher
		 */
		public static <T> JavaslangReactiveStreamsPublisher<T> ofAsync(Traversable<T> stream,Executor exec){
			return new JavaslangReactiveStreamsPublisher<T>(false,stream,exec);
		}
		/* 
		 *	@param s Reactive Streams subscriber
		 * @see org.reactivestreams.Publisher#subscribe(org.reactivestreams.Subscriber)
		 */
		@Override
		public void subscribe(Subscriber<? super T> s) {
			if(synchronous)
				ToStream.toFutureStreamFromTraversable(wrappedStream).sync().subscribe(s);
			else{
				new LazyReact(ThreadPools.getCurrentThreadExecutor()).withPublisherExecutor(exec).from(wrappedStream.iterator()).async().subscribe(s);
			}
				
				
		}

		
	}
