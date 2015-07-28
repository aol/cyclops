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

/**
 * Represents something that can generate a Stream, repeatedly
 * 
 * @author johnmcclean
 *
 * @param <T> Data type for Stream
 */
public interface Streamable<T> extends Iterable<T>{

	default Iterator<T> iterator(){
		return stream().iterator();
	}
	default  Object getStreamable(){
		return this;
	}
	
	/**
	 * @return SequenceM from this Streamable
	 */
	default SequenceM<T> sequenceM(){
		return SequenceM.fromStream(stream());
	}
	/**
	 * @return New Stream
	 */
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
	
	/**
	 * (Lazily) Construct a Streamable from a Stream.
	 * 
	 * @param stream to construct Streamable from
	 * @return Streamable
	 */
	public static <T> Streamable<T> fromStream(Stream<T> stream){
		return AsStreamable.asStreamable(stream);
	}
	/**
	 * (Lazily) Construct a Streamable from an Iterable.
	 * 
	 * @param iterable to construct Streamable from
	 * @return Streamable
	 */
	public static <T> Streamable<T> fromIterable(Iterable<T> iterable){
		return AsStreamable.asStreamable(iterable);
	}
	/**
	 * Construct a Streamable that returns an efficient Stream with the values reversed.
	 * 
	 * @param values to construct Stream from (reversed)
	 * @return (reversed) Streamable
	 */
	public static<T> Streamable<T> reveresedOf(T... values){
		
		return new Streamable<T>(){
			public Stream<T> stream(){
				return StreamUtils.reversedStream(Arrays.asList(values));
			}
		};
	}
	/**
	 * Construct a Streamable that returns an efficient Stream with the values in the 
	 * supplied list reversed
	 * 
	 * @param values to construct a Stream from (reversed)
	 * @return (reversed) Streamable
	 */
	public static<T> Streamable<T> reveresedOfList(ArrayList<T> values){
		
		return new Streamable<T>(){
			public Stream<T> stream(){
				return StreamUtils.reversedStream(values);
			}
		};
	}
	/**
	 * Construct a Streamable that returns a Stream
	 * 
	 * @param values to construct Streamable from
	 * @return Streamable
	 */
	public static<T> Streamable<T> of(T... values){
		
		return new Streamable<T>(){
			public Stream<T> stream(){
				return Stream.of(values);
			}
		};
	}
}
