package com.aol.cyclops.closures;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Stream;

import lombok.Value;



/**
 * Interface that represents a single value that can be converted into a List, Stream or Optional
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
public interface Convertable<T> extends Iterable<T>, Supplier<T>{
	
	/**
	 * Construct a Convertable from a Supplier
	 * 
	 * @param supplier That returns the convertable value
	 * @return Convertable
	 */
	public static <T>  Convertable<T> fromSupplier(Supplier<T> supplier){
		return new SupplierToConvertable<>(supplier);
	}
	
	
	@Value
	public static class SupplierToConvertable<T> implements Convertable<T>{
		private final Supplier<T> delegate;
		
		public T get(){
			return delegate.get();
		}
	}
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
	default <X extends Throwable> T orElseThrow(Supplier<? extends X> ex) throws X{
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
	
	/**
	 * @return A CompletableFuture, populated immediately by a call to get
	 */
	default CompletableFuture<T> toCompletableFuture(){
		return CompletableFuture.completedFuture(get());
	}
	/**
	 * @return A CompletableFuture populated asynchronously on the Common ForkJoinPool by calling get
	 */
	default CompletableFuture<T> toCompletableFutureAsync(){
		return CompletableFuture.supplyAsync(this);
	}
	/**
	 * @param exec Executor to asyncrhonously populate the CompletableFuture
	 * @return  A CompletableFuture populated asynchronously on the supplied Executor by calling get
	 */
	default CompletableFuture<T> toCompletableFutureAsync(Executor exec){
		return CompletableFuture.supplyAsync(this,exec);
	}
}
