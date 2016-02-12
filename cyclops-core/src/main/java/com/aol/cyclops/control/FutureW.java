package com.aol.cyclops.control;

import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

import com.aol.cyclops.Reducer;
import com.aol.cyclops.Semigroup;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.internal.matcher2.Case;
import com.aol.cyclops.internal.matcher2.CheckValues;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.types.ConvertableFunctor;
import com.aol.cyclops.types.FlatMap;
import com.aol.cyclops.types.ToAnyM;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.types.applicative.Applicativable;

import lombok.AllArgsConstructor;
@AllArgsConstructor
public class FutureW<T> implements ConvertableFunctor<T>,
											Applicativable<T>, 
											Value<T>, 
											FlatMap<T>,
											ToAnyM<T>{

	public static <T> FutureW<T> of(CompletableFuture<T> f){
		return new FutureW<>(f);
	}
	public static <T> FutureW<ListX<T>> sequence(CollectionX<FutureW<T>> fts){
		return AnyM.sequence(AnyM.<T>listFromFutureW(fts)).unwrap();
	}
	
	public static <T,R> FutureW<R> accumulate(CollectionX<FutureW<T>> fts,Reducer<R> reducer){
		return sequence(fts).map(s->s.mapReduce(reducer));
	}
	public static <T,R> FutureW<R> accumulate(CollectionX<FutureW<T>> fts,Function<? super T, R> mapper,Semigroup<R> reducer){
		return sequence(fts).map(s->s.map(mapper).reduce(reducer.reducer()).get());
	}

	//public static 
	private final CompletableFuture<T> future;

	@Override
	public <R> FutureW<R> map(Function<? super T, ? extends R> fn) {
		return new FutureW<R>(future.thenApply(fn));
	}
	@Override
	public <R> FutureW<R> patternMatch(R defaultValue,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> case1) {
		
		return (FutureW<R>)Applicativable.super.patternMatch(defaultValue, case1);
	}

	@Override
	public T get() {
		return future.join();
	}
	

	@Override
	public Iterator<T> iterator() {
		return toStream().iterator();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Unit#unit(java.lang.Object)
	 */
	@Override
	public <T> FutureW<T> unit(T unit) {
		return new FutureW<T>(CompletableFuture.completedFuture(unit));
	}

	@Override
	public SequenceM<T> stream() {
		return SequenceM.generate(()->get()).limit(1);
	}

	@Override
	public <R> FutureW<R> flatten() {
		return FutureW.of(AnyM.fromCompletableFuture(future).flatten().unwrap());
	}
	public <R> FutureW<R> flatMap(Function<? super T, ? extends CompletionStage<? extends R>> mapper){
		return FutureW.<R>of(future.<R>thenCompose(t->(CompletionStage)mapper.apply(t)));
		
	}
	
	public  Xor<Throwable,T> toXor(){
		try{
			return Xor.primary(future.join());
		}catch(Throwable t){
			return Xor.<Throwable,T>secondary(t);
		}
	}
	public  Ior<Throwable,T> toIor(){
		try{
			return Ior.primary(future.join());
		}catch(Throwable t){
			return Ior.<Throwable,T>secondary(t);
		}
	}
	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.closures.Convertable#toFutureW()
	 */
	@Override
	public FutureW<T> toFutureW() {
		return this;
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.closures.Convertable#toCompletableFuture()
	 */
	@Override
	public CompletableFuture<T> toCompletableFuture() {
		return this.future;
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.closures.Convertable#toCompletableFutureAsync()
	 */
	@Override
	public CompletableFuture<T> toCompletableFutureAsync() {
		return this.future;
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.closures.Convertable#toCompletableFutureAsync(java.util.concurrent.Executor)
	 */
	@Override
	public CompletableFuture<T> toCompletableFutureAsync(Executor exec) {
		return this.future;
	}
	
	public <R> FutureW<R> visit(Function<? super T, R> success, Function<Throwable, R> failure){
		return FutureW.of(future.thenApply(success).exceptionally(failure));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#cast(java.lang.Class)
	 */
	@Override
	public <U> FutureW<U> cast(Class<U> type) {
		
		return (FutureW<U>)Applicativable.super.cast(type);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#peek(java.util.function.Consumer)
	 */
	@Override
	public FutureW<T> peek(Consumer<? super T> c) {
		
		return (FutureW<T>)Applicativable.super.peek(c);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#trampoline(java.util.function.Function)
	 */
	@Override
	public <R> FutureW<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
		
		return(FutureW<R>)Applicativable.super.trampoline(mapper);
	}
	
	
}
