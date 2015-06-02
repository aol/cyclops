package com.aol.cyclops.streams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.AllArgsConstructor;
import lombok.Value;

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
	
	public static <T,A,R> List<R> collect(Stream<T> stream, Stream<Collector> collectors){
		return collect(stream, AsStreamable.<Collector>asStreamable(collectors));
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T,A,R> List<R> collect(Stream<T> stream, Iterable<Collector> collectors){
		return collect(stream, AsStreamable.<Collector>asStreamable(collectors));
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> List collect(Stream<T> stream, Streamable<Collector> collectors){
		
		
		final Supplier supplier =  ()-> collectors.stream().map(c->c.supplier().get()).collect(Collectors.toList());
		final BiConsumer accumulator = (acc,next) -> {  LazySeq.of(collectors.stream().iterator()).<Object,Pair<Collector,Object>>zip(LazySeq.of((List)acc),(a,b)->new Pair<Collector,Object>(a,b))
													
													.forEach( t -> t.v1().accumulator().accept(t.v2(),next));
		};
		final BinaryOperator combiner = (t1,t2)->  {
			Iterator t1It = ((Iterable)t1).iterator();
			Iterator t2It =  ((Iterable)t2).iterator();
			return collectors.stream().map(c->c.combiner().apply(t1It.next(),t2It.next())).collect(Collectors.toList());
		};
		Function finisher = t1 -> {
			Iterator t1It = ((Iterable)t1).iterator();
			return collectors.stream().map(c->c.finisher().apply(t1It.next())).collect(Collectors.toList());
		};
		 Collector col = Collector.of( supplier,accumulator , combiner,finisher);
			
		 return (List)stream.collect(col);
	}
	
	@Value @AllArgsConstructor
	public static class Pair<T1,T2>{
		T1 v1;
		T2 v2;
		public Pair(List list){
			v1 = (T1)list.get(0);
			v2 = (T2)list.get(1);
		}
		public T1 v1(){
			return v1;
		}
		public T2 v2(){
			return v2;
		}
	}
	
}
