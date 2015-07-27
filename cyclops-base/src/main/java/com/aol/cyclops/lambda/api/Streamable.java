package com.aol.cyclops.lambda.api;

import static com.aol.cyclops.lambda.api.AsDecomposable.asDecomposable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.aol.cyclops.lambda.monads.SequenceM;
import com.aol.cyclops.streams.StreamUtils;

public interface Streamable<T> extends Iterable<T>{

	default Iterator<T> iterator(){
		return stream().iterator();
	}
	default  Object getStreamable(){
		return this;
	}
	
	default SequenceM<T> sequenceM(){
		return SequenceM.fromStream(stream());
	}
	default Stream<T> stream(){
		Object streamable = getStreamable();
		if(streamable instanceof Stream)
			return (Stream)streamable;
		if(streamable instanceof Iterable)
			return StreamSupport.stream(((Iterable)streamable).spliterator(), false);
		return  new InvokeDynamic().stream(streamable).orElseGet( ()->
								(Stream)StreamSupport.stream(asDecomposable(streamable)
												.unapply()
												.spliterator(),
													false));
	}
	
	public static <T> Streamable<T> fromStream(Stream<T> stream){
		return AsStreamable.asStreamable(stream);
	}
	public static <T> Streamable<T> fromIterable(Iterable<T> iterable){
		return AsStreamable.asStreamable(iterable);
	}
	public static<T> Streamable<T> reveresedOf(T... values){
		
		return new Streamable<T>(){
			public Stream<T> stream(){
				return StreamUtils.reversedStream(Arrays.asList(values));
			}
		};
	}
	public static<T> Streamable<T> reveresedOfList(ArrayList<T> values){
		
		return new Streamable<T>(){
			public Stream<T> stream(){
				return StreamUtils.reversedStream(values);
			}
		};
	}
	public static<T> Streamable<T> of(T... values){
		
		return new Streamable<T>(){
			public Stream<T> stream(){
				return Stream.of(values);
			}
		};
	}
}
