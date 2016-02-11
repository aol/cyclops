package com.aol.cyclops.matcher2;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import lombok.val;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.cyclops.data.LazyImmutable;
import com.aol.cyclops.invokedynamic.ExceptionSoftener;
import com.aol.cyclops.invokedynamic.ReflectionCache;
import com.aol.cyclops.lambda.types.Decomposable;
import com.aol.cyclops.sequence.SequenceM;

/**
 * Generic extractors for use s pre and post data extractors.
 * 
 * @author johnmcclean
 *
 */
public class Extractors {
	
	
	
	
	/**
	 * An extractor that caches the extraction result
	 * 
	 * @param extractor to memoise (cache result of)
	 * @return Memoised extractor
	 */
	public static final <T,R > Extractor<T,R> memoised( Extractor<T,R> extractor){
		final LazyImmutable<R> value = new LazyImmutable<>();
		return input -> {
			return value.computeIfAbsent(()->extractor.apply(input));
				
		};
		
	}
	/**
	 * @return Extractor that decomposes Case classes into iterables 
	 */
	public static final <T,R> Extractor<T,R> decompose() {
		return input -> {
			if(input instanceof  Decomposable)
				return (R)((Decomposable)input).unapply();
			else if(input instanceof Iterable)
				return (R)input;
			return (R)input;
			
		};
	}
	private static <T,R> Function<T,R> unchecked(Unchecked<T,R> u){
	
			return t ->{ 
				try {
				return u.apply(t);
			}catch (Throwable e) {
				throw ExceptionSoftener.throwSoftenedException(e);
			}
			
		};
	}
	private static interface Unchecked<T,R>{
		public R apply(T t) throws Throwable;
	}
	/**
	 * An extractor that will generte a Tuple2 with two values at the specified index.
	 * Works on Iterable data structures.
	 * 
	 * @param v1 position of the first element to extract
	 * @param v2 position of the second element to extract
	 * @return Tuple with 2 specified elements
	 */
	public final static <V1,V2> Extractor<Iterable,Tuple2<V1,V2>> of(int v1,int v2){
		val l1 = new Long(v1);
		val l2 = new Long(v2);
		return  ( Iterable it)-> {
			
			List l  = (List)SequenceM.fromIterable(it).zipWithIndex().skip(Math.min(v1,v2))
					.limit(Math.max(v1,v2)+1)
					.filter(t -> ((Tuple2<Object,Long>)t).v2.equals(l1) || ((Tuple2<Object,Long>)t).v2.equals(l2))
					.map(t->((Tuple2)t).v1)
					.toList();
			return Tuple.tuple((V1)l.get(0),(V2)l.get(1));
		
			
		};
		
	}
	/**
	 * Return element or first element in an Iterable.
	 * 
	 * 
	 * @return value
	 */
	public final static <R> Extractor<?,R> first(){
	
		Extractor<Object,R> result =  ( item)-> {
			if(item instanceof Iterable)
				return (R)((Iterable)item).iterator().next();
			return (R)item;
		};
		return result;
		
	}
	/**
	 * Iterate to position and retieve value at position in an iterable data structure.
	 * 
	 * @param pos Position to extract
	 * @return value
	 */
	public final static <R> Extractor<Iterable<R>,R> at(int pos){
	
		return  ( Iterable<R> it)-> {
			return StreamSupport.stream(spliteratorUnknownSize(it.iterator(), ORDERED), false).skip(pos).limit(1).findFirst().get();
			
		};
		
	}
	/**
	 * 
	 * Look up element at position in a list
	 * 
	 * @param pos Position to extract element from
	 * @return Value at position
	 */
	public final static <R> Extractor<List<R>,R> get(int pos){
		return (List<R> c) ->{
			return c.get(pos);
		};
		
	}
	/**
	 * @param key Look up a value from a map
	 * @return Value for key specified
	 */
	public final static <K,R> Extractor<Map<K,R>,R> get(K key){
		return (Map<K,R> c) ->{
			return c.get(key);
		};
		
	}
	
	/**
	 * @return identity extractor, returns same value
	 */
	public final static <R>  Extractor<R,R> same(){
		return (R c) -> c;
	}
	
	
}
