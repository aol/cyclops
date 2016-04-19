package com.aol.cyclops.control;

import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducer;
import com.aol.cyclops.Semigroup;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.ConvertableFunctor;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.FlatMap;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.MonadicValue1;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.types.applicative.Applicativable;
import com.aol.cyclops.util.ExceptionSoftener;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
@AllArgsConstructor
@EqualsAndHashCode
public class FutureW<T> implements ConvertableFunctor<T>,
											Applicativable<T>, 
											MonadicValue1<T>, 
											FlatMap<T>,
											Filterable<T>{

    public static <T> FutureW<T> empty(){
        return new FutureW(CompletableFuture.completedFuture(null));
    }
    public static <T> FutureW<T> fromIterable(Iterable<T> iterable){
        Iterator<T> it = iterable.iterator();
        return FutureW.ofResult(Eval.fromIterable(iterable)).map(e->e.get());
    }
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
	public static <T> FutureW<T> accumulate(CollectionX<FutureW<T>> fts,Semigroup<T> reducer){
        return sequence(fts).map(s->s.reduce(reducer.reducer()).get());
    }

	public <R> Eval<R>  matches(Function<CheckValue1<T,R>,CheckValue1<T,R>> secondary,Function<CheckValue1<Throwable,R>,CheckValue1<Throwable,R>> primary,Supplier<? extends R> otherwise){
        return  toXor().swap().matches(secondary, primary, otherwise);
    }
	@Getter
	private final CompletableFuture<T> future;

	/* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#coflatMap(java.util.function.Function)
     */
    @Override
   public <R> FutureW<R> coflatMap(Function<? super MonadicValue<T>, R> mapper) {
        return (FutureW<R>)MonadicValue1.super.coflatMap(mapper);
    }
  
    /* cojoin
     * (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#nest()
     */
    @Override
    public  FutureW<MonadicValue<T>> nest(){
        return (FutureW<MonadicValue<T>>)MonadicValue1.super.nest();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue2#combine(com.aol.cyclops.Monoid, com.aol.cyclops.types.MonadicValue2)
     */
    @Override
    public FutureW<T> combine(Monoid<T> monoid, MonadicValue<? extends T> v2){
        return (FutureW<T>)MonadicValue1.super.combine(monoid,v2);
    }
	@Override
	public <R> FutureW<R> map(Function<? super T, ? extends R> fn) {
		return new FutureW<R>(future.thenApply(fn));
	}
	@Override
	public <R> FutureW<R> patternMatch(
			Function<CheckValue1<T, R>, CheckValue1<T, R>> case1,Supplier<? extends R> otherwise) {
		
		return (FutureW<R>)Applicativable.super.patternMatch(case1,otherwise);
	}

	@Override
	public T get() {
	    try{
	        return future.join();
	    }catch(Throwable t){
	        throw ExceptionSoftener.throwSoftenedException(t.getCause());
	    }
	}
	
	

	public boolean isSuccess(){
	    return future.isDone() && !future.isCompletedExceptionally();
	}
	public boolean isFailed(){
        return future.isCompletedExceptionally();
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
	public ReactiveSeq<T> stream() {
		return ReactiveSeq.generate(()->Try.withCatch(()->get()))
		                  .limit(1)
		                  .filter(t->t.isSuccess())
		                  .map(Value::get);
	}

	
	@Override
	public <R> FutureW<R> flatten() {
		return FutureW.of(AnyM.fromCompletableFuture(future).flatten().unwrap());
	}
	public <R> FutureW<R> flatMap(Function<? super T, ? extends MonadicValue<? extends R>> mapper){
		return FutureW.<R>of(future.<R>thenCompose(t->(CompletionStage<R>)mapper.apply(t).toFutureW().getFuture()));	
	}
	public <R> FutureW<R> flatMapCf(Function<? super T, ? extends CompletionStage<? extends R>> mapper){
        return FutureW.<R>of(future.<R>thenCompose(t->(CompletionStage<R>)mapper.apply(t))); 
    }
	
	public  Xor<Throwable,T> toXor(){
		try{
			return Xor.primary(future.join());
		}catch(Throwable t){
			return Xor.<Throwable,T>secondary(t.getCause());
		}
	}
	public  Ior<Throwable,T> toIor(){
		try{
			return Ior.primary(future.join());
		}catch(Throwable t){
			return Ior.<Throwable,T>secondary(t.getCause());
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
	public <U> FutureW<U> cast(Class<? extends U> type) {
		
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
	@Override
	public String toString() {
		return mkString();
	}
    public static <T> FutureW<T> ofResult(T result) {
       return FutureW.of(CompletableFuture.completedFuture(result));
    }
    public static <T> FutureW<T> ofError(Throwable error) {
        CompletableFuture<T> cf = new CompletableFuture<>();
        cf.completeExceptionally(error);
        
        return FutureW.<T>of(cf);
     }
    
   
    public String mkString(){
        return "FutureW["+future.toString()+"]";
    }
    @Override
    public Maybe<T> filter(Predicate<? super T> fn) {
        return toMaybe().filter(fn);
    }
    @Override
    public <U> Maybe<U> ofType(Class<? extends U> type) {
        
        return (Maybe<U>)Filterable.super.ofType(type);
    }
    @Override
    public Maybe<T> filterNot(Predicate<? super T> fn) {
       
        return (Maybe<T>)Filterable.super.filterNot(fn);
    }
    @Override
    public Maybe<T> notNull() {
       
        return (Maybe<T>)Filterable.super.notNull();
    }
    @Override
    public Optional<T> toOptional() {
        if(future.isDone() && future.isCompletedExceptionally())
            return Optional.empty();
        
        try{
            return Optional.ofNullable(get());
        }catch(Throwable t){
            return Optional.empty();
        }
        
    }
    public static <T> FutureW<T> ofSupplier(Supplier<T> s) {
       return FutureW.of(CompletableFuture.supplyAsync(s));
    }
   
    
	
	
}
