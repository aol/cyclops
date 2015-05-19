package com.aol.cyclops.streams;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.aol.cyclops.lambda.api.AsStreamable;
import com.aol.cyclops.lambda.api.Streamable;

public class StreamUtils {
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
	public static <U> Stream<U> cycle(Stream<U> s){
		return cycle(AsStreamable.asStreamable(s));
	}
	public static <U> Stream<U> cycle(Streamable<U> s){
		return Stream.iterate(s.stream(),s1-> s.stream()).flatMap(Function.identity());
	}
	
	public static <U> Stream<U> stream(Iterable<U> it){
		return StreamSupport.stream(it.spliterator(),
					false);
	}
	public static <U> Stream<U> stream(Iterator<U> it){
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED),
					false);
	}
	public static <K,V> Stream<Map.Entry<K, V>> stream(Map<K,V> it){
		return it.entrySet().stream();
	}
}
