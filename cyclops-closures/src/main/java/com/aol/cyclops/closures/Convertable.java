package com.aol.cyclops.closures;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Interface that represents a single value that can be converted into a List, Stream or Optional
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
public interface Convertable<T> extends Iterable<T>{

	public T get();
	default Optional<T> toOptional(){
		return Optional.ofNullable(get());
	}
	
	default Stream<T> toStream(){
		return Stream.of(get()).filter(v->v!=null);
	}
	
	/**Get the contained value or else the provided alternative
	 * 
	 * @param value
	 * @return
	 */
	default T orElse(T value){
		return toOptional().orElse(value);
	}
	
	/**
	 * Get the contained value or throw an exception if null
	 * 
	 * @param ex
	 * @return
	 * @throws X
	 */
	default <X extends Throwable> T getOrElseThrow(Supplier<? extends X> ex) throws X{
		return toOptional().orElseThrow(ex);
	}
	
	default List<T> toList(){
		return Arrays.asList(get());
	}
	
	default Iterator<T> iterator(){
		return toList().iterator();
	}
}
