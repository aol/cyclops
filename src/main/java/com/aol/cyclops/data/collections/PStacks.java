package com.aol.cyclops.data.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import org.pcollections.ConsPStack;
import org.pcollections.PStack;

import com.aol.cyclops.Reducer;
import com.aol.cyclops.Reducers;
/**
 * Class for working with Persistent Stacks from PCollections
 * Roughly Analogous to mutable LinkedLists
 * 
 * @author johnmcclean
 *
 */
public class PStacks {
	/**
	 * Construct a PStack from the provided values 
	 * 
	 * <pre>
	 * {@code 
	 *  List<String> list = PStacks.of("a","b","c");
	 *  
	 *  // or
	 *  
	 *  PStack<String> list = PStacks.of("a","b","c");
	 *  
	 *  
	 * }
	 * </pre>
	 * 
	 * 
	 * @param values To add to PStack
	 * @return new PStack
	 */
	public static <T> PStack<T> of(T...values){
		return ConsPStack.from(Arrays.asList(values));
	}
	/**
	 * <pre>
	 * {@code 
	 *  List<String> list = PStacks.of(Arrays.asList("a","b","c"));
	 *  
	 *  // or
	 *  
	 *  PStack<String> list = PStacks.of(Arrays.asList("a","b","c"));
	 *  
	 *  
	 * }
	 * 
	 * @param values To add to PStack
	 * @return
	 */
	public static <T> PStack<T> fromCollection(Collection<T> values){
		return ConsPStack.from(values);
	}
	/**
	 * <pre>
	 * {@code 
	 *     List<String> empty = PStack.empty();
	 *    //or
	 *    
	 *     PStack<String> empty = PStack.empty();
	 * }
	 * </pre>
	 * @return an empty PStack
	 */
	public static <T> PStack<T> empty(){
		return  ConsPStack.empty();
	}
	/**
	 * Construct a PStack containing a single value
	 * </pre>
	 * {@code 
	 *    List<String> single = PStacks.singleton("1");
	 *    
	 *    //or
	 *    
	 *    PStack<String> single = PStacks.singleton("1");
	 * 
	 * }
	 * </pre>
	 * 
	 * @param value Single value for PVector
	 * @return PVector with a single value
	 */
	public static <T> PStack<T> singleton(T value){
		return ConsPStack.singleton(value);
	}
	/**
	 * Reduce (immutable Collection) a Stream to a PStack, note for efficiency reasons,
	 * the produced PStack is reversed (this is more efficient than the non-reversing equivalents)
	 * 
	 * 
	 * <pre>
	 * {@code 
	 *    PStack<Integer> list = PStacks.fromStream(Stream.of(1,2,3));
	 * 
	 *  //list = [3,2,1]
	 * }</pre>
	 * 
	 * 
	 * @param stream to convert to a PVector
	 * @return
	 */
	public static<T> PStack<T> fromStreamReversed(Stream<T> stream){
		return (PStack<T>)toPStackReverse().mapReduce(stream);
	}
	public static<T> PStack<T> fromStream(Stream<T> stream){
		return (PStack<T>)toPStack().mapReduce(stream);
	}
	/**
	 * Return a reducer that can produce a PStack from a Stream, note this is more efficient than
	 * the unreversed equivalent
	 * 
	 * <pre>
	 * {@code 
	 *   PStack<String> list = SequenceM.of("a","b","c").mapReduce(PStacks.toPStack()
	 *   //list = ["a","b","c"]
	 *   PStack<String> list = PStacks.toPStack().reduce(Stream.of("a","b","c")));
	 *    //list = ["a","b","c"]
	 * }
	 * </pre>
	 * 
	 * @return a Monoid / Reducer that can be used to convert a Stream to a PVector
	 */
	public static <T> Reducer<PStack<T>> toPStackReverse() { 
		return	Reducers.toPStackReversed();
	}
	public static <T> Reducer<PStack<T>> toPStack() { 
		return	Reducers.toPStack();
	}
	
}
