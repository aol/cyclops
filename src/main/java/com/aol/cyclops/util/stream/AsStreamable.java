package com.aol.cyclops.util.stream;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

import com.aol.cyclops.internal.stream.SeqUtils;
import com.aol.cyclops.internal.stream.StreamableImpl;

import lombok.AllArgsConstructor;


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
	
	private static <T> Iterable<T> collectStreamConcurrent(T object){
		if(object instanceof Stream){
			
			Collection c = SeqUtils.toConcurrentLazyCollection((Stream)object);
			 return new PrintableIterable<T>(c);
		}
		if(object instanceof Object[]){
            return (Iterable<T>)Arrays.asList((Object[])object);
        }
		if(object instanceof Iterable)
            return (Iterable<T>)object;
        
        return Arrays.asList(object);
	}
	
	private static <T> Iterable<T> collectStream(T object){
		if(object instanceof Stream){
			
			Collection c = SeqUtils.toLazyCollection((Stream)object);
			return new PrintableIterable<T>(c);		
		}
		if(object instanceof Object[]){
		    return (Iterable<T>)Arrays.asList((Object[])object);
		}
		if(object instanceof Iterable)
		    return (Iterable<T>)object;
		
		return Arrays.asList(object);
	}
	
	@AllArgsConstructor
	static class PrintableIterable<T> implements Iterable<T>{
	    private final Collection c;
	    @Override
        public Iterator<T> iterator() {
            return c.iterator();
        }
        
	    public String toString(){
	        return String.format("%s", c );
	    }
	}
	
}
