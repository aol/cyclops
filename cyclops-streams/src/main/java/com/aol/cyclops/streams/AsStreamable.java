package com.aol.cyclops.streams;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.Value;

import com.aol.cyclops.sequence.Streamable;

public class AsStreamable {
	public static <T> Streamable<T> asStreamable(Object toCoerce){
		return new CoercedStreamable(collectStream(toCoerce));
	}
	/**
	 * @param toCoerce Efficiently / lazily Makes Stream repeatable, not thread safe, on initial iteration
	 * @return
	 */
	public static <T> Streamable<T> asStreamable(Stream<T> toCoerce){
		return new CoercedStreamable(collectStream(toCoerce));
	}
	public static <T> Streamable<T> asStreamable(Iterable<T> toCoerce){
		return new CoercedStreamable(collectStream(toCoerce));
	}
	/**
	 * @param toCoerce Efficiently / lazily Makes Stream repeatable, guards iteration with locks on initial iteration
	 * @return
	 */
	public static <T> Streamable<T> asConcurrentStreamable(Stream<T> toCoerce){
		return new CoercedStreamable(collectStreamConcurrent(toCoerce));
	}
	public static <T> Streamable<T> asConcurrentStreamable(Iterable<T> toCoerce){
		return new CoercedStreamable(collectStreamConcurrent(toCoerce));
	}
	
	private static <T> T collectStreamConcurrent(T object){
		if(object instanceof Stream){
			
			Collection c = StreamUtils.toLazyCollection((Stream)object);
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
			
			Collection c = StreamUtils.toLazyCollection((Stream)object);
			return (T)new Iterable(){

				@Override
				public Iterator iterator() {
					return c.iterator();
				}
				
		};
		}
		return object;
	}
	@Value
	public static class CoercedStreamable<T> implements StreamableMixin<T>{
		@Getter
		private final T streamable;
		
	}
}
