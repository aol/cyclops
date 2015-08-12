package com.aol.simple.react.stream.pushable;

import java.util.stream.Stream;

import org.jooq.lambda.Seq;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.Topic;
import com.aol.simple.react.stream.lazy.LazyReact;
import com.aol.simple.react.stream.traits.LazyFutureStream;
import com.aol.simple.react.threads.ReactPool;

/**
 * Build Streams that stream data from the topic instance
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
public class MultiplePushableStreamsBuilder<T>  {
	
	private final Topic<T> topic;

	
	MultiplePushableStreamsBuilder(Queue<T> q){
		topic = new Topic(q);
	}
	
	/**
	 * Create a pushable LazyFutureStream using the supplied ReactPool
	 * 
	 * @param s ReactPool to use to create the Stream
	 * @return a Tuple2 with a Topic&lt;T&gt; and LazyFutureStream&lt;T&gt; - add data to the Queue
	 * to push it to the Stream
	 */
	public  LazyFutureStream<T> pushable(ReactPool<LazyReact> s){
		
		return s.nextReactor().of(topic.stream());
		
	}

	/**
	 * Create a pushable JDK 8 Stream
	 * @return a Tuple2 with a Topic&lt;T&gt; and Stream&lt;T&gt; - add data to the Queue
	 * to push it to the Stream
	 */
	public   Stream<T> pushableStream(){
		
		return (Stream)topic.stream();
		
	}
	/**
	 * Create a pushable org.jooq.lambda.Seq
	 * 
	 * @return a Tuple2 with a Topic&lt;T&gt; and Seq&lt;T&gt; - add data to the Queue
	 * to push it to the Stream
	 */
	public Seq<T> pushableSeq(){
		
		return topic.stream();
	}
	
	/**
	 * Create a pushable LazyFutureStream. This will call LazyFutureStream#futureStream(Stream) which creates
	 * a sequential LazyFutureStream
	 * 
	  @return a Tuple2 with a Queue&lt;T&gt; and LazyFutureStream&lt;T&gt; - add data to the Queue
	 * to push it to the Stream
	 */
	public  <T> LazyFutureStream<T> pushableLazyFutureStream(){
		
		return LazyFutureStream.lazyFutureStream((Stream<T>)topic.stream());
	}

	/**
	 * @return Topic used as input for any generated Streams
	 */
	public Topic<T> getInput() {
		return topic;
	}

}
