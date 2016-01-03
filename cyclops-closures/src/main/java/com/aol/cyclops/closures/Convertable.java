package com.aol.cyclops.closures;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
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

	/**
	 * @return Contained value, maybe null
	 */
	public T get();
	/**
	 * @return Optional that wraps contained value, Optional.empty if value is null
	 */
	default Optional<T> toOptional(){
		return Optional.ofNullable(get());
	}
	
	/**
	 * @return Stream containing value returned by get(), Empty Stream if null
	 */
	default Stream<T> toStream(){
		return Stream.of(get()).filter(v->v!=null);
	}
	
	/**
	 * @return An AtomicReference containing value returned by get()
	 */
	default AtomicReference<T> toAtomicReference(){
		return new AtomicReference<T>(get());
	}
	/**
	 * @return An Optional AtomicReference containing value returned by get(), Optional.empty if get() returns null
	 */
	default Optional<AtomicReference<T>> toOptionalAtomicReference(){
		return toOptional().map(u->new AtomicReference<T>(u));
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
	
	/**
	 * @return A List containing value returned by get(), if get() returns null an Empty List is returned
	 */
	default List<T> toList(){
		T obj = get();
		if(obj!=null)
			return Arrays.asList(get());
		return Arrays.asList();
	}
	
	
	/* An Iterator over the list returned from toList()
	 * 
	 *  (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	default Iterator<T> iterator(){
		return toList().iterator();
	}
}
