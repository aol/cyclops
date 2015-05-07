package com.aol.cyclops.trycatch;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.val;

import com.aol.cyclops.lambda.utils.ExceptionSoftener;

@AllArgsConstructor
public class Failure<T,X extends Throwable> implements Try<T,X> {

	private final X error;
	
	public static <T,X extends Throwable> Failure<T,X> of(X error){
		return new Failure<>(error);
	}
	public T get(){
		throw new RuntimeException(error);
	}

	@Override
	public <R> Try<R,X> map(Function<T, R> fn) {
		return (Failure)this;
	}

	@Override
	public <R> Try<R,X> flatMap(Function<T, Try<R,X>> fn) {
		return (Try)this;
	}
	@Override
	public Try<T,X> filter(Predicate<T> p) {
		return this;
	}
	
	@Override
	public Try<T,X> recoverWithFor(Class<? extends X> t,Function<X, Success<T,X>> fn){
		if(error.getClass().isAssignableFrom(t))
			return recoverWith(fn);
		return this;
	}
	@Override
	public Try<T,X> recoverFor(Class<? extends X> t,Function<X, T> fn){
		if(error.getClass().isAssignableFrom(t))
			return recover(fn);
		return this;
	}
	
	@Override
	public Success<T,X> recover(Function<X, T> fn) {
		return Success.of(fn.apply(error));
	}
	
	@Override
	public  Success<T,X> recoverWith(Function<X,Success<T,X>> fn){
		return fn.apply(error);
	}
	@Override
	public Try<T,X> flatten() {
		return this;
	}
	@Override
	public T orElse(T value) {
		return value;
	}
	@Override
	public T orElseGet(Supplier<T> value) {
		return get();
	}
	@Override
	public Optional<T> toOptional() {
		return Optional.empty();
	}
	@Override
	public Stream<T> toStream() {
		return Stream.of();
	}
	@Override
	public boolean isSuccess() {
		return false;
	}
	@Override
	public boolean isFailure() {
		return true;
	}
	@Override
	public void foreach(Consumer<T> consumer) {
		
		
	}
	@Override
	public Try<T,X> onFail(Consumer<X> consumer) {
		consumer.accept(error);
		return this;
	}
	@Override
	public Try<T, X> onFail(Class<? extends X> t, Consumer<X> consumer) {
		if(error.getClass().isAssignableFrom(t))
			consumer.accept(error);
		return this;
	}
	@Override
	public void throwException() {
		ExceptionSoftener.singleton.factory.getInstance().throwSoftenedException(error);
		
	}
	@Override
	public Optional<X> toFailedOptional() {
		
		return Optional.of(error);
	}
	@Override
	public Stream<X> toFailedStream() {
		return Stream.of(error);
	}
	@Override
	public void foreachFailed(Consumer<X> consumer) {
		consumer.accept(error);
		
	}
}
