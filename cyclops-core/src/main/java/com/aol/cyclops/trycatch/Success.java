package com.aol.cyclops.trycatch;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.aol.cyclops.collections.extensions.standard.ListX;
import com.aol.cyclops.invokedynamic.ExceptionSoftener;
import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.sequence.SequenceM;

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
	private final Class<? extends Throwable>[] classes;
	
	@Override
	public ListX<T> unapply() {
		return ListX.of(value);
	}

	
	
	/* 
	 *	@return Current value
	 * @see com.aol.cyclops.trycatch.Try#get()
	 */
	public T get(){
		return value;
	}
	/**
	 * @return This monad wrapped as AnyM
	 */
	public AnyM<T> anyM(){
		return AnyM.fromIterable(this);
	}
	/**
	 * @return This monad, wrapped as AnyM of Failure
	 */
	public AnyM<X> anyMFailure(){
		return AnyM.fromOptional(Optional.empty());
	}
	/**
	 * @return This monad, wrapped as AnyM of Success
	 */
	public AnyM<T> anyMSuccess(){
		return anyM();
	}
	/**
	 * @param value Successful value
	 * @return new Success with value
	 */
	@Deprecated //use Try.success instead
	public static <T,X extends Throwable> Success<T,X> of(T value){
		return new Success<>(value,new Class[0]);
	}
	/**
	 * @param value Successful value
	 * @return new Success with value
	 */
	public static <T,X extends Throwable> AnyM<T> anyMOf(T value,Class<? extends Throwable>[] classes){
		return new Success<>(value,classes).anyM();
	}
	/**
	 * @param value Successful value
	 * @return new Success with value
	 */
	public static <T,X extends Throwable> AnyM<T> anyMOf(T value){
		return new Success<>(value,new Class[0]).anyM();
	}
	/**
	 * @param value Successful value
	 * @return new Success with value
	 */
	public static <T,X extends Throwable> Success<T,X> of(T value,Class<? extends Throwable>[] classes){
		return new Success<>(value,classes);
	}
	
	/* 
	 * @param fn Map success value from T to R.
	 * @return New Try with mapped value
	 * @see com.aol.cyclops.trycatch.Try#map(java.util.function.Function)
	 */
	@Override
	public <R> Try<R,X> map(Function<? super T,? extends R> fn) {
		return safeApply( ()->of(fn.apply(get())));
	}
	
	private <R> R safeApply(Supplier<R> s){
		try{
			return s.get();
		}catch(Throwable t){
			return (R)Failure.of(orThrow(Stream.of(classes).filter(c->c.isAssignableFrom(t.getClass())).map(c->t).findFirst(),t));
			
		}
	}

	private Throwable orThrow(Optional<Throwable> findFirst, Throwable t) {
		if(findFirst.isPresent())
			return findFirst.get();
		ExceptionSoftener.throwSoftenedException(t);
		return null;
	}



	/* 
	 * @param fn FlatMap success value or Do nothing if Failure (return this)
	 * @return Try returned from FlatMap fn
	 * @see com.aol.cyclops.trycatch.Try#flatMap(java.util.function.Function)
	 */
	@Override
	public <R> Try<R,X> flatMap(Function<? super T,? extends Try<R,X>> fn) {
		return safeApply(()-> fn.apply(get()));
		
	}

	/* 
	 * @param p Convert a Success to a Failure (with a null value for Exception) if predicate does not hold.
	 *         
	 * @return this if  Predicate holds, new Failure if not
	 * @see com.aol.cyclops.trycatch.Try#filter(java.util.function.Predicate)
	 */
	@Override
	public Optional<T> filter(Predicate<? super T> p) {
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
	public Success<T,X> recover(Function<? super X,? extends T> fn) {
		return this;
	}

	/* 
	 * Does nothing (no error to recover from)
	 * @see com.aol.cyclops.trycatch.Try#recoverWith(java.util.function.Function)
	 */
	@Override
	public Success<T,X> recoverWith(Function<? super X,? extends Success<T,X>> fn) {
		return this;
	}

	/* 
	 * Does nothing (no error to recover from)
	 * @see com.aol.cyclops.trycatch.Try#recoverFor(java.lang.Class, java.util.function.Function)
	 */
	@Override
	public Success<T,X> recoverFor(Class<? extends X> t, Function<? super X, ? extends T> fn) {
		return this;
	}

	/* 
	 * Does nothing (no error to recover from)
	 * @see com.aol.cyclops.trycatch.Try#recoverWithFor(java.lang.Class, java.util.function.Function)
	 */
	@Override
	public Success<T,X> recoverWithFor(Class<? extends X> t,
			Function<? super X,? extends Success<T,X>> fn) {
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
	public T orElseGet(Supplier<? extends T> value) {
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
	public SequenceM<T> stream() {
		return SequenceM.<T>of();
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
	public void forEach(Consumer<? super T> consumer) {
		consumer.accept(value);
		
	}

	/* 
	 *  does nothing no failure
	 * @see com.aol.cyclops.trycatch.Try#onFail(java.util.function.Consumer)
	 */
	@Override
	public Try<T,X> onFail(Consumer<? super X> consumer) {
		return this;
	}

	/* 
	 *  does nothing no failure
	 * @see com.aol.cyclops.trycatch.Try#onFail(java.lang.Class, java.util.function.Consumer)
	 */
	@Override
	public Try<T, X> onFail(Class<? extends X> t, Consumer<X> consumer) {
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
	public void forEachFailed(Consumer<? super X> consumer) {
		
		
	}



	/* (non-Javadoc)
	 * @see com.aol.cyclops.trycatch.Try#when(java.util.function.Function, java.util.function.Function)
	 */
	@Override
	public <R> R when(Function<? super T, ? extends R> success, Function<? super X, ? extends R> failure) {
		return success.apply(get());
	}
	
}
