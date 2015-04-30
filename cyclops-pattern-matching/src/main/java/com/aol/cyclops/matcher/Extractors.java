package com.aol.cyclops.matcher;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import lombok.val;

import org.jooq.lambda.Seq;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.jooq.lambda.tuple.Tuple5;
import org.jooq.lambda.tuple.Tuple6;
import org.jooq.lambda.tuple.Tuple7;
import org.jooq.lambda.tuple.Tuple8;


/**
 * Generic extractors for use s pre and post data extractors.
 * 
 * @author johnmcclean
 *
 */
public class Extractors {
	
	private static final Object NOT_SET = new Object();
	
	private static final Map<Class,Function> decomposers= new HashMap<>();
	
	/**
	 * Register decomposition function in standard hashmap
	 * @param c
	 * @param f
	 */
	public static final void registerDecompositionFunction(Class c, Function f){
		decomposers.put(c, f);
	}
	public static final <T,R > Extractor<T,R> memoised( Extractor<T,R> extractor){
		Object[] value = {NOT_SET};
		return input -> {
				if(value[0]==NOT_SET )
					value[0]=extractor.apply(input);
				return (R)value[0];
		};
		
	}
	public static final <T,R> Extractor<T,R> decompose() {
		return input -> {
			if(input instanceof  Decomposable)
				return (R)((Decomposable)input).unapply();
			else if(decomposers.get(input.getClass())!=null)
				return (R)decomposers.get(input.getClass()).apply(input);
			
			return (R)ReflectionCache.getUnapplyMethod(input.getClass()).map(Unchecked.function(m->m.invoke(input))).orElse(input);
			
		};
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
		
			List l  = (List)Seq.seq(it).zipWithIndex().skip(Math.min(v1,v2))
								.limit(Math.max(v1,v2)+1)
								.filter(t -> ((Tuple2<Object,Long>)t).v2.equals(l1) || ((Tuple2<Object,Long>)t).v2.equals(l2))
								.map(t->((Tuple2)t).v1)
								.collect(Collectors.toList());
			return Tuple.tuple((V1)l.get(0),(V2)l.get(1));
			
		};
		
	}
	/**
	 * Return element or first element in an Iterable.
	 * 
	 * @param pos Position to extract
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
	
	public final static <R>  Extractor<R,R> same(){
		return (R c) -> c;
	}
	
	public final static <V1,V2> Extractor<Iterable,Tuple2<V1,V2>> toTuple2(){
		return  ( Iterable itable)-> {
			Iterator it = itable.iterator();
			return new Tuple2(it.next(),it.next());
		};
		
	}
	public final static <V1,V2,V3> Extractor<Iterable,Tuple3<V1,V2,V3>> toTuple3(){
		return  ( Iterable itable)-> {
			Iterator it = itable.iterator();
			return new Tuple3(it.next(),it.next(),it.next());
		};
		
	}
	public final static <V1,V2,V3,V4> Extractor<Iterable,Tuple4<V1,V2,V3,V4>> toTuple4(){
		return  ( Iterable itable)-> {
			Iterator it = itable.iterator();
			return new Tuple4(it.next(),it.next(),it.next(),it.next());
		};
		
	}
	public final static <V1,V2,V3,V4,V5> Extractor<Iterable,Tuple5<V1,V2,V3,V4,V5>> toTuple5(){
		return  ( Iterable itable)-> {
			Iterator it = itable.iterator();
			return new Tuple5(it.next(),it.next(),it.next(),it.next(),it.next());
		};
		
	}
	public final static <V1,V2,V3,V4,V5,V6> Extractor<Iterable,Tuple6<V1,V2,V3,V4,V5,V6>> toTuple6(){
		return  ( Iterable itable)-> {
			Iterator it = itable.iterator();
			return new Tuple6(it.next(),it.next(),it.next(),it.next(),it.next(),it.next());
		};
		
	}
	public final static <V1,V2,V3,V4,V5,V6,V7> Extractor<Iterable,Tuple7<V1,V2,V3,V4,V5,V6,V7>> toTuple7(){
		return  ( Iterable itable)-> {
			Iterator it = itable.iterator();
			return new Tuple7(it.next(),it.next(),it.next(),it.next(),it.next(),it.next(),it.next());
		};
		
	}
	public final static <V1,V2,V3,V4,V5,V6,V7,V8> Extractor<Iterable,Tuple8<V1,V2,V3,V4,V5,V6,V7,V8>> toTuple8(){
		return  ( Iterable itable)-> {
			Iterator it = itable.iterator();
			return new Tuple8(it.next(),it.next(),it.next(),it.next(),it.next(),it.next(),it.next(),it.next());
		};
		
	}
	
}
