package com.aol.cyclops.trycatch;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Class that represents a Successful Try
 * 
 * @author johnmcclean
 *
 * @param <T> Success data type
 * @param <X> Error data type
 */
@RequiredArgsConstructor @ToString @EqualsAndHashCode
public class Success<T, X extends Throwable> implements Try<T,X>{
	
	private final T value;
	
	@Override
	public <R extends Iterable<?>> R unapply() {
		return (R)Arrays.asList(value);
	}

	
	
	/* 
	 *	@return Current value
	 * @see com.aol.cyclops.trycatch.Try#get()
	 */
	public T get(){
		return value;
	}

	/**
	 * @param value Successful value
	 * @return new Success with value
	 */
	public static <T,X extends Throwable> Success<T,X> of(T value){
		return new Success<>(value);
	}
	/* 
	 * @param fn Map success value from T to R.
	 * @return New Try with mapped value
	 * @see com.aol.cyclops.trycatch.Try#map(java.util.function.Function)
	 */
	@Override
	public <R> Try<R,X> map(Function<T, R> fn) {
		return of(fn.apply(get()));
	}

	/* 
	 * @param fn FlatMap success value or Do nothing if Failure (return this)
	 * @return Try returned from FlatMap fn
	 * @see com.aol.cyclops.trycatch.Try#flatMap(java.util.function.Function)
	 */
	@Override
	public <R> Try<R,X> flatMap(Function<T, Try<R,X>> fn) {
		return fn.apply(get());
	}

	/* 
	 * @param p Convert a Success to a Failure (with a null value for Exception) if predicate does not hold.
	 *         
	 * @return this if  Predicate holds, new Failure if not
	 * @see com.aol.cyclops.trycatch.Try#filter(java.util.function.Predicate)
	 */
	@Override
	public Optional<T> filter(Predicate<T> p) {
		if(p.test(value))
			return Optional.of(get());
		else
			return Optional.empty();
	}

	/* 
	 * Does nothing (no error to recover from)
	 * @see com.aol.cyclops.trycatch.Try#recover(java.util.function.Function)
	 */
	@Override
	public Success<T,X> recover(Function<X, T> fn) {
		return this;
	}

	/* 
	 * Does nothing (no error to recover from)
	 * @see com.aol.cyclops.trycatch.Try#recoverWith(java.util.function.Function)
	 */
	@Override
	public Success<T,X> recoverWith(Function<X, Success<T,X>> fn) {
		return this;
	}

	/* 
	 * Does nothing (no error to recover from)
	 * @see com.aol.cyclops.trycatch.Try#recoverFor(java.lang.Class, java.util.function.Function)
	 */
	@Override
	public Success<T,X> recoverFor(Class<? super X> t, Function<X, T> fn) {
		return this;
	}

	/* 
	 * Does nothing (no error to recover from)
	 * @see com.aol.cyclops.trycatch.Try#recoverWithFor(java.lang.Class, java.util.function.Function)
	 */
	@Override
	public Success<T,X> recoverWithFor(Class<? super X> t,
			Function<X, Success<T,X>> fn) {
		return this;
	}

	/* 
	 * Flatten a nested Try Structure
	 * @return Lowest nested Try
	 * @see com.aol.cyclops.trycatch.Try#flatten()
	 */
	@Override
	public Try<T,X> flatten() {
		if(value instanceof Try)
			return ((Try)value).flatten();
		return this;
	}

	/* 
	 *	
	 *	@return Returns current value (ignores supplied value)
	 * @see com.aol.cyclops.trycatch.Try#orElse(java.lang.Object)
	 */
	@Override
	public T orElse(T value) {
		return get();
	}

	/* 
	 *	@param value (ignored)
	 *	@return Returns current value (ignores Supplier)
	 * @see com.aol.cyclops.trycatch.Try#orElseGet(java.util.function.Supplier)
	 */
	@Override
	public T orElseGet(Supplier<T> value) {
		return get();
	}

	/* 
	 *	@return Optional of current value
	 * @see com.aol.cyclops.trycatch.Try#toOptional()
	 */
	@Override
	public Optional<T> toOptional() {
		return Optional.of(value);
	}

	/* 
	 *	@return Stream of current value
	 * @see com.aol.cyclops.trycatch.Try#toStream()
	 */
	@Override
	public Stream<T> stream() {
		return Stream.of(value);
	}

	/* 
	 *	@return true
	 * @see com.aol.cyclops.trycatch.Try#isSuccess()
	 */
	@Override
	public boolean isSuccess() {
		return true;
	}

	/* 
	 *	@return false
	 * @see com.aol.cyclops.trycatch.Try#isFailure()
	 */
	@Override
	public boolean isFailure() {
		return false;
	}

	/* 
	 *	@param consumer to recieve current value
	 * @see com.aol.cyclops.trycatch.Try#foreach(java.util.function.Consumer)
	 */
	@Override
	public void foreach(Consumer<T> consumer) {
		consumer.accept(value);
		
	}

	/* 
	 *  does nothing no failure
	 * @see com.aol.cyclops.trycatch.Try#onFail(java.util.function.Consumer)
	 */
	@Override
	public Try<T,X> onFail(Consumer<X> consumer) {
		return this;
	}

	/* 
	 *  does nothing no failure
	 * @see com.aol.cyclops.trycatch.Try#onFail(java.lang.Class, java.util.function.Consumer)
	 */
	@Override
	public Try<T, X> onFail(Class<? super X> t, Consumer<X> consumer) {
		return this;
	}

	/* 
	 *  does nothing no failure
	 * @see com.aol.cyclops.trycatch.Try#throwException()
	 */
	@Override
	public void throwException() {
		
		
	}

	/* 
	 *  @return java.util.Optional#empty()
	 * @see com.aol.cyclops.trycatch.Try#toFailedOptional()
	 */
	@Override
	public Optional<X> toFailedOptional() {
		return Optional.empty();
	}

	/* 
	 *	@return empty Stream
	 * @see com.aol.cyclops.trycatch.Try#toFailedStream()
	 */
	@Override
	public Stream<X> toFailedStream() {
		return Stream.of();
	}

	/* 
	 *	does nothing - no failure
	 * @see com.aol.cyclops.trycatch.Try#foreachFailed(java.util.function.Consumer)
	 */
	@Override
	public void foreachFailed(Consumer<X> consumer) {
		
		
	}
	
}
