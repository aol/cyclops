package com.aol.cyclops.collections;

import java.util.Collection;
import java.util.stream.Stream;

import org.pcollections.AmortizedPQueue;
import org.pcollections.PQueue;

import com.aol.cyclops.Reducer;
import com.aol.cyclops.sequence.Reducers;

public class PQueues {
	/**
	 * Construct a PQueue from the provided values 
	 * 
	 * <pre>
	 * {@code 
	 *  List<String> q = PQueues.of("a","b","c");
	 *  
	 *  // or
	 *  
	 *  PStack<String> q = PQueues.of("a","b","c");
	 *  
	 *  
	 * }
	 * </pre>
	 * 
	 * 
	 * @param values To add to PQueue
	 * @return new PQueue
	 */
	public static <T> PQueue<T> of(T...values){
		
		PQueue<T> result = empty();
		for(T value : values){
			result = result.plus(value);
		}
		return result;
		
	}
	/**
	 * <pre>
	 * {@code 
	 *     List<String> empty = PQueue.empty();
	 *    //or
	 *    
	 *     PQueue<String> empty = PQueue.empty();
	 * }
	 * </pre>
	 * @return an empty PQueue
	 */
	public static <T> PQueue<T> empty(){
		return AmortizedPQueue.empty();
	}
	/**
	 * Construct a PQueues containing a single value
	 * </pre>
	 * {@code 
	 *    List<String> single = PQueues.singleton("1");
	 *    
	 *    //or
	 *    
	 *    PQueues<String> single = PQueues.singleton("1");
	 * 
	 * }
	 * </pre>
	 * 
	 * @param value Single value for PQueue
	 * @return PQueue with a single value
	 */
	public static <T> PQueue<T> singleton(T value){
		PQueue<T> result = empty();
		result = result.plus(value);
		return result;
	}
	/**
	 * <pre>
	 * {@code 
	 *  List<String> list = PQueues.of(Arrays.asList("a","b","c"));
	 *  
	 *  // or
	 *  
	 *  PQueues<String> list = PQueues.of(Arrays.asList("a","b","c"));
	 *  
	 *  
	 * }
	 * 
	 * @param values To add to PQueue
	 * @return
	 */
	public static<T> PQueue<T> fromCollection(Collection<T> stream){
		if(stream instanceof PQueue)
			return (PQueue)(stream);
		return  PQueues.<T>empty().plusAll(stream);
		
	}
	/**
	 * Reduce (immutable Collection) a Stream to a PStack, note for efficiency reasons,
	 * the produced PStack is reversed.
	 * 
	 * 
	 * <pre>
	 * {@code 
	 *    PQueue<Integer> q = PQueues.fromStream(Stream.of(1,2,3));
	 * 
	 *  //queue = [3,2,1]
	 * }</pre>
	 * 
	 * 
	 * @param stream to convert to a PVector
	 * @return
	 */
	public static<T> PQueue<T> fromStream(Stream<T> stream){
		return (PQueue<T>)toPQueue().mapReduce(stream);
	}
	/**
	 * Return a reducer that can produce a PStack from a Stream, note for efficiency reasons,
	 * the produced PStack will be reversed.
	 * 
	 * <pre>
	 * {@code 
	 *   PQueue<String> q = SequenceM.of("a","b","c").mapReduce(PQueues.toPStack()
	 *   //queue = ["a","b","c"]
	 *   PQueues<String> q = PQueues.toPStack().reduce(Stream.of("a","b","c")));
	 *    //queue = ["a","b","c"]
	 * }
	 * </pre>
	 * 
	 * @return a Monoid / Reducer that can be used to convert a Stream to a PVector
	 */
	public static <T> Reducer<PQueue<T>> toPQueue() { 
		return	Reducers.toPQueue();
	}
}
