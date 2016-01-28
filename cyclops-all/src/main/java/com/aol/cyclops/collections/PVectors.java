package com.aol.cyclops.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import org.pcollections.ConsPStack;
import org.pcollections.PStack;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import com.aol.cyclops.collections.extensions.persistent.PVectorX;
import com.aol.cyclops.collections.extensions.persistent.PVectorXImpl;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.Reducers;
import com.aol.cyclops.sequence.SequenceM;

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
	public static <T> PVectorX<T> of(T...values){
		return new PVectorXImpl<>(TreePVector.from(Arrays.asList(values)));
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
	public static<T> PVectorX<T> empty(){
		return new PVectorXImpl<>(TreePVector .empty());
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
	public static <T> PVectorX<T> singleton(T value){
		return new PVectorXImpl<>(TreePVector.singleton(value));
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
	public static <T> PVectorX<T> fromCollection(Collection<T> values){
		return new PVectorXImpl<>(TreePVector.from(values));
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
	public static<T> PVectorX<T> fromStream(Stream<T> stream){
		return new PVectorXImpl<>((PVector<T>)toPVector().mapReduce(stream));
	}
	/**
	 * 
	 * <pre>
	 * {@code 
	 *   PVector<String> list = SequenceM.of("a","b","c").mapReduce(PVectors.toPVector()
	 *   //list = ["a","b","c"]
	 *   PVector<String> list = PVectors.toPVector().reduce(Stream.of("a","b","c")));
	 *    //list = ["a","b","c"]
	 * }
	 * </pre>
	 * 
	 * @return a Monoid / Reducer that can be used to convert a Stream to a PVector
	 */
	public static <T> Monoid<PVector<T>> toPVector() { 
		return	Reducers.toPVector();
	}
}
