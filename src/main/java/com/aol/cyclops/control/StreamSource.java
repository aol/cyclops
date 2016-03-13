package com.aol.cyclops.control;

import java.util.Objects;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;

import org.jooq.lambda.Seq;

import com.aol.cyclops.data.async.Adapter;
import com.aol.cyclops.data.async.Queue;
import com.aol.cyclops.data.async.QueueFactories;
import com.aol.cyclops.data.async.QueueFactory;
import com.aol.cyclops.react.threads.ReactPool;
import com.aol.cyclops.types.futurestream.LazyFutureStream;
import com.aol.cyclops.util.stream.pushable.MultipleStreamSource;
import com.aol.cyclops.util.stream.pushable.PushableLazyFutureStream;
import com.aol.cyclops.util.stream.pushable.PushableReactiveSeq;
import com.aol.cyclops.util.stream.pushable.PushableStream;

/**
 * Create Java 8 Streams that data can be pushed into
 * 
 * @author johnmcclean
 *
 */

@AllArgsConstructor(access=AccessLevel.PRIVATE)
public class StreamSource{

	private final int backPressureAfter;
	private final boolean backPressureOn;

	/**
     * @return a builder that will use Topics to allow multiple Streams from the same data
     */
    public static <T> MultipleStreamSource<T> ofMultiple(){
        return new MultipleStreamSource<T>(StreamSource.ofUnbounded().createQueue());
    }
    /**
     * @return a builder that will use Topics to allow multiple Streams from the same data
     */
    public static <T> MultipleStreamSource<T> ofMultiple(int backPressureAfter){
        return new MultipleStreamSource<T>(StreamSource.of(backPressureAfter).createQueue());
    }
    /**
     * @return a builder that will use Topics to allow multiple Streams from the same data
     */
    public static <T> MultipleStreamSource<T> ofMultiple(QueueFactory<?> q){
        Objects.requireNonNull(q);
        return new MultipleStreamSource<T>(StreamSource.of(q).createQueue());
    }
	public static  StreamSource of(QueueFactory<?> q){
	    Objects.requireNonNull(q);
	    return new StreamSource(){
	        @SuppressWarnings("unchecked")
            @Override
	        <T> Queue<T> createQueue (){
	            return (Queue<T>)q.build();
	            
	        }
	    };
	}
	public static  StreamSource ofUnbounded(){
	    return new StreamSource();
	}
	public static  StreamSource of(int backPressureAfter){
	    if(backPressureAfter<1)
	        throw new IllegalArgumentException("Can't apply back pressure after less than 1 event");
        return new StreamSource(backPressureAfter,true);
    }
	
	<T> Queue<T> createQueue (){
		
			Queue q;
			if(!backPressureOn)
					q = QueueFactories.unboundedNonBlockingQueue().build();
			else
				q = QueueFactories.boundedQueue(backPressureAfter).build();
			return q;
	}

	private StreamSource(){
			
			this.backPressureAfter= Runtime.getRuntime().availableProcessors();
			this.backPressureOn=false;
	}
	
	/**
	 * Create a pushable LazyFutureStream using the supplied ReactPool
	 * 
	 * @param s ReactPool to use to create the Stream
	 * @return a Tuple2 with a Queue&lt;T&gt; and LazyFutureStream&lt;T&gt; - add data to the Queue
	 * to push it to the Stream
	 */
	public  <T>  PushableLazyFutureStream<T> futureStream(LazyReact s){
		
		Queue<T> q = createQueue();
		return new PushableLazyFutureStream<T>(q,s.fromStream(q.stream()));
		
	}
	/**
     * Create a LazyFutureStream. his will call LazyFutureStream#futureStream(Stream) which creates
     * a sequential LazyFutureStream
     * 
     * @param adapter Adapter to create a LazyFutureStream from
     * @return A LazyFutureStream that will accept values from the supplied adapter
     */
    public static <T> LazyFutureStream<T> futureStream(Adapter<T> adapter, LazyReact react){
        
        return react.fromStream(adapter.stream());
    }

	/**
	 * Create a pushable JDK 8 Stream
	 * @return a Tuple2 with a Queue&lt;T&gt; and Stream&lt;T&gt; - add data to the Queue
	 * to push it to the Stream
	 */
	public  <T> PushableStream<T> stream(){
		Queue<T> q = createQueue();
		return new PushableStream<T>(q,(Stream)q.stream());
		
	}
	/**
	 * Create a pushable org.jooq.lambda.Seq
	 * 
	 * @return a Tuple2 with a Queue&lt;T&gt; and Seq&lt;T&gt; - add data to the Queue
	 * to push it to the Stream
	 */
	public <T> PushableReactiveSeq<T> reactiveSeq(){
		Queue<T> q = createQueue();
		return new PushableReactiveSeq<T>(q,q.stream());
	}
	
	

	/**
	 * Create a JDK 8 Stream from the supplied Adapter
	 * 
	 * @param adapter Adapter to create a Steam from
	 * @return Stream that will accept input from supplied adapter
	 */
	public  static <T> Stream<T> stream(Adapter<T> adapter){
		
		return adapter.stream();
	}
	/**
	 * Create a pushable org.jooq.lambda.Seq
	 * 
	 * @param adapter Adapter to create a Seq from
	 * @return A Seq that will accept input from a supplied adapter
	 */
	public static <T> ReactiveSeq<T> reactiveSeq(Adapter<T> adapter){
		
		return adapter.stream();
	}
	
	
	
	
	
}
