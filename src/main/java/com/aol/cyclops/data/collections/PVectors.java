package com.aol.cyclops.data.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import org.pcollections.PVector;
import org.pcollections.TreePVector;

import com.aol.cyclops.Reducer;
import com.aol.cyclops.Reducers;

/**
 * Class for working with Persistent Vectors from PCollections
 * Roughly Analogous to mutable ArrayLists
 * 
 * @author johnmcclean
 *
 */
public class PVectors {

	/**
	 * Construct a PVector from the provided values 
	 * 
	 * <pre>
	 * {@code 
	 *  List<String> list = PVectors.of("a","b","c");
	 *  
	 *  // or
	 *  
	 *  PVector<String> list = PVectors.of("a","b","c");
	 *  
	 *  
	 * }
	 * </pre>
	 * 
	 * 
	 * @param values To add to PVector
	 * @return new PVector
	 */
	public static <T> PVector<T> of(T...values){
		return TreePVector.from(Arrays.asList(values));
	}
	/**
	 * <pre>
	 * {@code 
	 *     List<String> empty = PVectors.empty();
	 *    //or
	 *    
	 *     PVector<String> empty = PVectors.empty();
	 * }
	 * </pre>
	 * @return an empty PVector
	 */
	public static<T> PVector<T> empty(){
		return TreePVector .empty();
	}
	/**
	 * Construct a PVector containing a single value
	 * </pre>
	 * {@code 
	 *    List<String> single = PVectors.singleton("1");
	 *    
	 *    //or
	 *    
	 *    PVector<String> single = PVectors.singleton("1");
	 * 
	 * }
	 * </pre>
	 * 
	 * @param value Single value for PVector
	 * @return PVector with a single value
	 */
	public static <T> PVector<T> singleton(T value){
		return  TreePVector.singleton(value);
	}
	/**
	 * Create a PVector from the supplied Colleciton
	 * <pre>
	 * {@code 
	 *   PVector<Integer> list = PVectors.fromCollection(Arrays.asList(1,2,3));
	 *   
	 * }
	 * </pre>
	 * 
	 * @param values to add to new PVector
	 * @return PVector containing values
	 */
	public static <T> PVector<T> fromCollection(Collection<T> values){
		return TreePVector.from(values);
	}
	/**
	 * Reduce (immutable Collection) a Stream to a PVector
	 * 
	 * <pre>
	 * {@code 
	 *    PVector<Integer> list = PVectors.fromStream(Stream.of(1,2,3));
	 * 
	 *  //list = [1,2,3]
	 * }</pre>
	 * 
	 * 
	 * @param stream to convert to a PVector
	 * @return
	 */
	public static<T> PVector<T> fromStream(Stream<T> stream){
		return (PVector<T>)toPVector().mapReduce(stream);
	}
	/**
	 * 
	 * <pre>
	 * {@code 
	 *   PVector<String> list = ReactiveSeq.of("a","b","c").mapReduce(PVectors.toPVector()
	 *   //list = ["a","b","c"]
	 *   PVector<String> list = PVectors.toPVector().reduce(Stream.of("a","b","c")));
	 *    //list = ["a","b","c"]
	 * }
	 * </pre>
	 * 
	 * @return a Monoid / Reducer that can be used to convert a Stream to a PVector
	 */
	public static <T> Reducer<PVector<T>> toPVector() { 
		return	Reducers.toPVector();
	}
}
