package com.aol.cyclops.control;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

import com.aol.cyclops.functions.caching.Memoize;
import com.aol.cyclops.lambda.monads.Functor;
import com.aol.cyclops.value.Value;


/**
 * Represents a computation that can be defered, cached or immediate
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
public interface Eval<T> extends Supplier<T>, Value<T>, Functor<T>{

	public static<T> Eval<T> now(T value){
		return new Now<T>(value);
	}
	public static<T> Eval<T> later(Supplier<T> value){
		
		return new Later<T>(in->value.get());
	}
	public static<T> Eval<T> always(Supplier<T> value){
		return new Always<T>(in->value.get());
	}
	
	public <R> Eval<R> map(Function<? super T, ? extends R> mapper);
	public <R> Eval<R> flatMap(Function<? super T, ? extends Eval<R>> mapper);
	
	public T get();
	default Eval<CompletableFuture<T>> asyncNow(Executor ex){
		return Eval.now(this.toCompletableFutureAsync(ex));
	}
	default Eval<CompletableFuture<T>> asyncNow(){
		return Eval.now(this.toCompletableFuture());
	}
	default Eval<CompletableFuture<T>> asyncLater(Executor ex){
		return Eval.later(()->this.toCompletableFutureAsync(ex));
	}
	default Eval<CompletableFuture<T>> asyncLater(){
		return Eval.later(()->this.toCompletableFutureAsync());
	}
	default Eval<CompletableFuture<T>> asyncAlways(Executor ex){
		return Eval.always(()->this.toCompletableFutureAsync(ex));
	}
	default Eval<CompletableFuture<T>> asyncAlways(){
		return Eval.always(()->this.toCompletableFutureAsync());
	}
	
	static class Now<T> implements Eval<T>{
		private final T value;
		Now(T value){
			this.value = value;
		}
		public <R> Eval<R> map(Function<? super T, ? extends R> mapper){
			return new Now<>(mapper.apply(value));
		}
		public <R> Eval<R> flatMap(Function<? super T, ? extends Eval<R>> mapper){
			return mapper.apply(value);
		}
		@Override
		public T get() {
			return value;
		}
		
	}
	
	
	static class Later<T> implements Eval<T>{
		private final Function<?,? extends T> s;
		Later(Function <?,? extends T> s){
			this.s = Memoize.memoizeFunction(s);
		}
		public <R> Eval<R> map(Function<? super T, ? extends R> mapper){
			//unncessary cast
			return new Later<R>(mapper.compose((Function)s));
		}
		public <R>  Eval<R> flatMap(Function<? super T, ? extends Eval<R>> mapper){
			return  Eval.later(()->((Eval<R>)((Function)mapper).compose(s).apply("")).get());
		}
		@Override
		public T get() {
			return (T)((Function)s).apply("");
		}
		
		
	}
	static class Always<T> implements Eval<T>{
		private final Function<?,? extends T> s;
		Always(Function <?,? extends T> s){
			this.s = s;
		}
		public <R> Eval<R> map(Function<? super T, ? extends R> mapper){
			//unncessary cast
			return new Later<R>(mapper.compose((Function)s));
			
		}
		public <R>  Eval<R> flatMap(Function<? super T, ? extends Eval<R>> mapper){
			return  Eval.always(()->((Eval<R>)((Function)mapper).compose(s).apply("")).get());
		}
		@Override
		public T get() {
			return s.apply(null);
		}
	}
}
