package com.aol.simple.react.stream.pushable;

import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.experimental.Wither;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.simple.react.async.Adapter;
import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.QueueFactories;
import com.aol.simple.react.stream.eager.EagerFutureStream;
import com.aol.simple.react.stream.eager.EagerReact;
import com.aol.simple.react.stream.lazy.LazyFutureStream;
import com.aol.simple.react.stream.lazy.LazyReact;
import com.aol.simple.react.threads.ReactPool;

/**
 * Create Java 8 Streams that data can be pushed into
 * 
 * @author johnmcclean
 *
 */
@Wither
@AllArgsConstructor
public class PushableStreamBuilder{

	private final int backPressureAfter;
	private final boolean backPressureOn;

	private <T> Queue<T> createQueue (){
		
			Queue q;
			if(!backPressureOn)
					q = new Queue<>();
			else
				q = QueueFactories.boundedQueue(backPressureAfter).build();
			return q;
	}

	public PushableStreamBuilder(){
			
			this.backPressureAfter= Runtime.getRuntime().availableProcessors();
			this.backPressureOn=false;
	}
	
	/**
	 * Create a pushable LazyFutureStream using the supplied ReactPool
	 * 
	 * @param s ReactPool to use to create the Stream
	 * @return a Tuple2 with a Queue<T> and LazyFutureStream<T> - add data to the Queue
	 * to push it to the Stream
	 */
	public  <T>  PushableLazyFutureStream<T> pushable(ReactPool<LazyReact> s){
		
		Queue<T> q = createQueue();
		return new PushableLazyFutureStream<T>(q,s.nextReactor().fromStreamWithoutFutures(q.stream()));
		
	}

	/**
	 * Create a pushable JDK 8 Stream
	 * @return a Tuple2 with a Queue<T> and Stream<T> - add data to the Queue
	 * to push it to the Stream
	 */
	public  <T>  PushableStream<T> pushableStream(){
		Queue<T> q = createQueue();
		return new PushableStream<T>(q,(Stream)q.stream());
		
	}
	/**
	 * Create a pushable org.jooq.lambda.Seq
	 * 
	 * @return a Tuple2 with a Queue<T> and Seq<T> - add data to the Queue
	 * to push it to the Stream
	 */
	public<T>  PushableSeq<T> pushableSeq(){
		Queue<T> q = createQueue();
		return new PushableSeq<T>(q,q.stream());
	}
	
	/**
	 * Create a pushable LazyFutureStream. This will call LazyFutureStream#futureStream(Stream) which creates
	 * a sequential LazyFutureStream
	 * 
	  @return a Tuple2 with a Queue<T> and LazyFutureStream<T> - add data to the Queue
	 * to push it to the Stream
	 */
	public <T>  PushableLazyFutureStream<T> pushableLazyFutureStream(){
		Queue<T> q = createQueue();
		return new PushableLazyFutureStream<T>(q,LazyFutureStream.futureStream((Stream<T>)q.stream()));
	}

	/**
	 * Create a JDK 8 Stream from the supplied Adapter
	 * 
	 * @param adapter Adapter to create a Steam from
	 * @return Stream that will accept input from supplied adapter
	 */
	public  <T> Stream<T> pushableStream(Adapter<T> adapter){
		
		return adapter.stream();
	}
	/**
	 * Create a pushable org.jooq.lambda.Seq
	 * 
	 * @param adapter Adapter to create a Seq from
	 * @return A Seq that will accept inout from a supplied adapter
	 */
	public <T> Seq<T> pushableSeq(Adapter<T> adapter){
		
		return adapter.stream();
	}
	
	/**
	 * Create a LazyFutureStream. his will call LazyFutureStream#futureStream(Stream) which creates
	 * a sequential LazyFutureStream
	 * 
	 * @param adapter Adapter to create a LazyFutureStream from
	 * @return A LazyFutureStream that will accept values from the supplied adapter
	 */
	public <T> LazyFutureStream<T> pushableLazyFutureStream(Adapter<T> adapter){
		
		return LazyFutureStream.futureStream((Stream<T>)adapter.stream());
	}
	
	public <T> MultiplePushableStreamsBuilder<T> multiple(){
		return new MultiplePushableStreamsBuilder<T>(createQueue());
	}
	
}
