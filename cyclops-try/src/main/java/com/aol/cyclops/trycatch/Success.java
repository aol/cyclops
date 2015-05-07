package com.aol.cyclops.trycatch;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Success<T, X extends Throwable> implements Try<T,X>{
	
	private final T value;
	
	public T get(){
		return value;
	}

	public static <T,X extends Throwable> Success<T,X> of(T value){
		return new Success<>(value);
	}
	@Override
	public <R> Try<R,X> map(Function<T, R> fn) {
		return of(fn.apply(get()));
	}

	@Override
	public <R> Try<R,X> flatMap(Function<T, Try<R,X>> fn) {
		return fn.apply(get());
	}

	@Override
	public Try<T,X> filter(Predicate<T> p) {
		if(p.test(value))
			return this;
		else
			return Failure.of(null);
	}

	@Override
	public Success<T,X> recover(Function<X, T> fn) {
		return this;
	}

	@Override
	public Success<T,X> recoverWith(Function<X, Success<T,X>> fn) {
		return this;
	}

	@Override
	public Success<T,X> recoverFor(Class<? extends X> t, Function<X, T> fn) {
		return this;
	}

	@Override
	public Success<T,X> recoverWithFor(Class<? extends X> t,
			Function<X, Success<T,X>> fn) {
		return this;
	}

	@Override
	public Try<T,X> flatten() {
		if(value instanceof Try)
			return ((Try)value).flatten();
		return this;
	}

	@Override
	public T orElse(T value) {
		return get();
	}

	@Override
	public T orElseGet(Supplier<T> value) {
		return get();
	}

	@Override
	public Optional<T> toOptional() {
		return Optional.of(value);
	}

	@Override
	public Stream<T> toStream() {
		return Stream.of(value);
	}

	@Override
	public boolean isSuccess() {
		return true;
	}

	@Override
	public boolean isFailure() {
		return false;
	}

	@Override
	public void foreach(Consumer<T> consumer) {
		consumer.accept(value);
		
	}

	@Override
	public Try<T,X> onFail(Consumer<X> consumer) {
		return this;
	}

	@Override
	public Try<T, X> onFail(Class<? extends X> t, Consumer<X> consumer) {
		return this;
	}

	@Override
	public void throwException() {
		
		
	}

	@Override
	public Optional<X> toFailedOptional() {
		return Optional.empty();
	}

	@Override
	public Stream<X> toFailedStream() {
		return Stream.of();
	}

	@Override
	public void foreachFailed(Consumer<X> consumer) {
		
		
	}
	
}
