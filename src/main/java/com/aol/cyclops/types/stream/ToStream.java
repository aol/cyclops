package com.aol.cyclops.types.stream;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.aol.cyclops.internal.invokedynamic.InvokeDynamic;
import com.aol.cyclops.internal.matcher2.AsDecomposable;
import com.aol.cyclops.internal.stream.ReversedIterator;
import com.aol.cyclops.internal.stream.SeqUtils;
import com.aol.cyclops.control.ReactiveSeq;

public interface ToStream<T> extends Iterable<T>,ConvertableToReactiveSeq<T>{
	default Iterator<T> iterator(){
		return stream().iterator();
	}
	default  Object getStreamable(){
		return this;
	}
	default ReactiveSeq<T> reveresedSequenceM(){
		return ReactiveSeq.fromStream(reveresedStream());
	}
	/**
	 * @return SequenceM from this Streamable
	 */
	default ReactiveSeq<T> reactiveSeq(){
		return ReactiveSeq.fromStream(new FromStreamable<T>().stream(getStreamable()));
	}
	default Stream<T> reveresedStream(){
		Object streamable = getStreamable();
		if(streamable instanceof List){
			return StreamSupport.stream(new ReversedIterator((List)streamable).spliterator(),false);
		}
		if(streamable instanceof Object[]){
			List arrayList = Arrays.asList((Object[])streamable);
			return StreamSupport.stream(new ReversedIterator(arrayList).spliterator(),false);
		}
		return SeqUtils.reverse(new FromStreamable<T>().stream(getStreamable()));
	}
	default boolean isEmpty(){
		return this.reactiveSeq().isEmpty();
	}
	default Stream<T> stream(){
		Object streamable = getStreamable();
		if(streamable instanceof Stream)
			return (Stream)streamable;
		if(streamable instanceof Iterable)
			return StreamSupport.stream(((Iterable)streamable).spliterator(), false);
		return  new InvokeDynamic().stream(streamable).orElseGet( ()->
								(Stream)StreamSupport.stream(AsDecomposable.asDecomposable(streamable)
												.unapply()
												.spliterator(),
													false));
	}
	static class FromStreamable<T>{
	/**
	 * @return New Stream
	 */
		public Stream<T> stream(Object streamable){
			
		if(streamable instanceof Stream)
			return (Stream)streamable;
		if(streamable instanceof Iterable)
			return StreamSupport.stream(((Iterable)streamable).spliterator(), false);
		return  new InvokeDynamic().stream(streamable).orElseGet( ()->
								(Stream)StreamSupport.stream(AsDecomposable.asDecomposable(streamable)
												.unapply()
												.spliterator(),
													false));
		}
	}
	
}
