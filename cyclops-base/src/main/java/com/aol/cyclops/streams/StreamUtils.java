package com.aol.cyclops.streams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.aol.cyclops.lambda.api.AsStreamable;
import com.aol.cyclops.lambda.api.Monoid;
import com.aol.cyclops.lambda.api.Streamable;
import com.nurkiewicz.lazyseq.LazySeq;

public interface StreamUtils {
	/**
	 * Reverse a Stream
	 * 
	 * @param stream Stream to reverse
	 * @return Reversed stream
	 */
	public static <U> Stream<U> reverse(Stream<U> stream){
		return reversedStream(stream.collect(Collectors.toList()));
	}
	/**
	 * Create a reversed Stream from a List
	 * 
	 * @param list List to create a reversed Stream from
	 * @return Reversed Stream
	 */
	public static <U> Stream<U> reversedStream(List<U> list){
		return new ReversedIterator<>(list).stream();
	}
	/**
	 * Create a new Stream that infiniteable cycles the provided Stream
	 * @param s Stream to cycle
	 * @return New cycling stream
	 */
	public static <U> Stream<U> cycle(Stream<U> s){
		return cycle(AsStreamable.asStreamable(s));
	}
	/**
	 * Create a Stream that infiniteable cycles the provided Streamable
	 * @param s Streamable to cycle
	 * @returnNew cycling stream
	 */
	public static <U> Stream<U> cycle(Streamable<U> s){
		return Stream.iterate(s.stream(),s1-> s.stream()).flatMap(Function.identity());
	}
	
	/**
	 * Create a Stream that infiniteable cycles the provided Streamable
	 * @param s Streamable to cycle
	 * @returnNew cycling stream
	 */
	public static <U> Stream<U> cycle(int times,Streamable<U> s){
		return Stream.iterate(s.stream(),s1-> s.stream()).limit(times).flatMap(Function.identity());
	}
	
	/**
	 * Create a stream from an iterable
	 * 
	 * @param it Iterable to convert to a Stream
	 * @return Stream from iterable
	 */
	public static <U> Stream<U> stream(Iterable<U> it){
		return StreamSupport.stream(it.spliterator(),
					false);
	}
	/**
	 * Create a stream from an iterator
	 * 
	 * @param it Iterator to convert to a Stream
	 * @return Stream from iterator
	 */
	public static <U> Stream<U> stream(Iterator<U> it){
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED),
					false);
	}
	
	
	public static <U> Stream<U> concat(Object o, Stream<U> stream){
		Stream<U> first = null;
		if(o instanceof Stream){
			first = (Stream)o;
		}else if(o instanceof Iterable){
			first = stream( (Iterable)o);
		}
		else{
			first = Stream.of((U)o);
		}
		return Stream.concat(first, stream);
		
	}
	/**
	 * Create a stream from a map
	 * 
	 * @param it Iterator to convert to a Stream
	 * @return Stream from a map
	 */
	public static <K,V> Stream<Map.Entry<K, V>> stream(Map<K,V> it){
		return it.entrySet().stream();
	}
	@SuppressWarnings({"rawtypes","unchecked"})
	public static <R> List<R> reduce(Stream<R> stream,Iterable<Monoid<R>> reducers){
	
		Monoid<R> m = new Monoid(){
			public List zero(){
				return stream(reducers).map(r->r.zero()).collect(Collectors.toList());
			}
			public BiFunction<List,List,List> combiner(){
				return (c1,c2) -> { 
					List l= new ArrayList<>();
					int i =0;
					for(Monoid next : reducers){
						l.add(next.combiner().apply(c1.get(i),c2.get(0)));
						i++;
					}
					
					
					return l;
				};
			}
			
		
			public Stream mapToType(Stream stream){
				return (Stream) stream.map(value->Arrays.asList(value));
			}
		};
		return (List)m.mapReduce(stream);
	}
	@SuppressWarnings({"rawtypes","unchecked"})
	public static <R> List<R> reduce(Stream<R> stream,Stream<Monoid<R>> reducer){
		return (List)reduce(stream, (List)reducer.collect(Collectors.toList()));
		
	}
	
	
	
}
