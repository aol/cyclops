package com.aol.cyclops.util.stream;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.Value;

import com.aol.cyclops.internal.sequence.streamable.StreamableImpl;
import com.aol.cyclops.sequence.SeqUtils;


public class AsStreamable {
	public static <T> Streamable<T> fromObject(Object toCoerce){
		return new StreamableImpl(collectStream(toCoerce));
	}
	/**
	 * @param toCoerce Efficiently / lazily Makes Stream repeatable, not thread safe, on initial iteration
	 * @return
	 */
	public static <T> Streamable<T> fromStream(Stream<T> toCoerce){
		return new StreamableImpl(collectStream(toCoerce));
	}
	public static <T> Streamable<T> fromIterable(Iterable<T> toCoerce){
		return new StreamableImpl(collectStream(toCoerce));
	}
	/**
	 * @param toCoerce Efficiently / lazily Makes Stream repeatable, guards iteration with locks on initial iteration
	 * @return
	 */
	public static <T> Streamable<T> synchronizedFromStream(Stream<T> toCoerce){
		return new StreamableImpl(collectStreamConcurrent(toCoerce));
	}
	public static <T> Streamable<T> synchronizedFromIterable(Iterable<T> toCoerce){
		return new StreamableImpl(collectStreamConcurrent(toCoerce));
	}
	
	private static <T> T collectStreamConcurrent(T object){
		if(object instanceof Stream){
			
			Collection c = SeqUtils.toConcurrentLazyCollection((Stream)object);
			return (T)new Iterable(){

				@Override
				public Iterator iterator() {
					return c.iterator();
				}
				
		};
		}
		return object;
	}
	
	private static <T> T collectStream(T object){
		if(object instanceof Stream){
			
			Collection c = SeqUtils.toLazyCollection((Stream)object);
			return (T)new Iterable(){

				@Override
				public Iterator iterator() {
					return c.iterator();
				}
				
		};
		}
		return object;
	}
	
}
