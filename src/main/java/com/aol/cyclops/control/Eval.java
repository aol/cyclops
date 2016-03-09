package com.aol.cyclops.control;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.aol.cyclops.Reducer;
import com.aol.cyclops.Semigroup;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.types.applicative.Applicativable;
import com.aol.cyclops.util.function.Memoize;





/**
 * Represents a computation that can be defered, cached or immediate
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
public interface Eval<T> extends Supplier<T>, MonadicValue<T>, Functor<T>, Filterable<T>, Applicativable<T>{

    
	public static<T> Eval<T> now(T value){
		return new Now<T>(value);
	}
	public static<T> Eval<T> later(Supplier<T> value){
		
		return new Later<T>(in->value.get());
	}
	public static<T> Eval<T> always(Supplier<T> value){
		return new Always<T>(in->value.get());
	}
	
	public static <T> Eval<ListX<T>> sequence(CollectionX<Eval<T>> evals){
		return AnyM.sequence(AnyM.<T>listFromEval(evals)).unwrap();
	}
	
	public static <T,R> Eval<R> accumulate(CollectionX<Eval<T>> evals,Reducer<R> reducer){
		return sequence(evals).map(s->s.mapReduce(reducer));
	}
	public static <T,R> Eval<R> accumulate(CollectionX<Eval<T>> maybes,Function<? super T, R> mapper,Semigroup<R> reducer){
		return sequence(maybes).map(s->s.map(mapper).reduce(reducer.reducer()).get());
	}
	public static <T> Eval<T> accumulate(CollectionX<Eval<T>> maybes,Semigroup<T> reducer){
        return sequence(maybes).map(s->s.reduce(reducer.reducer()).get());
    }
	public <T> Eval<T> unit(T unit);
	public <R> Eval<R> map(Function<? super T, ? extends R> mapper);
	public <R> Eval<R> flatMap(Function<? super T, ? extends Eval<? extends R>> mapper);
	
	
	
	
	public T get();
	
	
	@Override
    default <U> Maybe<U> ofType(Class<U> type) {
       
        return (Maybe<U>)Filterable.super.ofType(type);
    }
    @Override
    default Maybe<T> filterNot(Predicate<? super T> fn) {
       
        return (Maybe<T>)Filterable.super.filterNot(fn);
    }
    @Override
    default Maybe<T> notNull() {
       
        return (Maybe<T>)Filterable.super.notNull();
    }
    default Maybe<T> filter(Predicate<? super T> pred){
	    return toMaybe().filter(pred);
	}
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#cast(java.lang.Class)
	 */
	@Override
	default <U> Eval<U> cast(Class<U> type) {
		return (Eval<U>)Applicativable.super.cast(type);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#peek(java.util.function.Consumer)
	 */
	@Override
	default Eval<T> peek(Consumer<? super T> c) {
		return (Eval<T>)Applicativable.super.peek(c);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#trampoline(java.util.function.Function)
	 */
	@Override
	default <R> Eval<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
		
		return (Eval<R>)Applicativable.super.trampoline(mapper);
	}
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
	static <R> Eval<R> narrow(Eval<? extends R> broad){
		return (Eval<R>)broad;
	}
	public static class Now<T> implements Eval<T>{
		private final T value;
		Now(T value){
			this.value = value;
		}
		public <R> Eval<R> map(Function<? super T, ? extends R> mapper){
			return new Now<>(mapper.apply(value));
		}
		public <R> Eval<R> flatMap(Function<? super T, ? extends Eval<? extends R>> mapper){
			return narrow(mapper.apply(value));
		}
		@Override
		public T get() {
			return value;
		}
		@Override
		public <T> Eval<T> unit(T unit) {
			return Eval.now(unit);
		}
		/* (non-Javadoc)
		 * @see com.aol.cyclops.value.Value#toEvalNow()
		 */
		@Override
		public Eval<T> toEvalNow() {
			return this;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return get().hashCode();
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof Eval))
				return false;
			return Objects.equals(get(), ((Eval)obj).get());
		}
		
		@Override
		public String toString(){
			return mkString();
		}
		
	}
	
	public static class Later<T> implements Eval<T>{
		private final Function<Object,? extends T> s;
		private final static Object VOID = new Object();
		Later(Function <Object,? extends T> s){
			this.s = Memoize.memoizeFunction(s);
		}
		public <R> Eval<R> map(Function<? super T, ? extends R> mapper){
			return new Later<R>(mapper.compose(s));
		}
		public <R>  Eval<R> flatMap(Function<? super T, ? extends Eval<? extends R>> mapper){
			
			return  Eval.later(()->mapper.compose(s).apply(VOID).get());
		}
		@Override
		public T get() {
			return s.apply(VOID);
		}
		/* (non-Javadoc)
		 * @see com.aol.cyclops.lambda.monads.Unit#unit(java.lang.Object)
		 */
		@Override
		public <T> Eval<T> unit(T unit) {
			return Eval.later(()->unit);
		}
		/* (non-Javadoc)
		 * @see com.aol.cyclops.value.Value#toEvalLater()
		 */
		@Override
		public Eval<T> toEvalLater() {
			return this;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return get().hashCode();
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof Eval))
				return false;
			return Objects.equals(get(), ((Eval)obj).get());
		}
		@Override
		public String toString(){
			return mkString();
		}
		
		
		
	}
	public static class Always<T> implements Eval<T>{
		private final Function<Object,? extends T> s;
		Always(Function <Object,? extends T> s){
			this.s = s;
		}
		public <R> Eval<R> map(Function<? super T, ? extends R> mapper){
			
			return new Always<R>(mapper.compose(s));
			
		}
		public <R>  Eval<R> flatMap(Function<? super T, ? extends Eval<? extends R>> mapper){
			return  Eval.always(()->mapper.compose(s).apply(null).get());
		}
		@Override
		public T get() {
			return s.apply(null);
		}
		@Override
		public <T> Eval<T> unit(T unit) {
			return Eval.always(()->unit);
		}
		/* (non-Javadoc)
		 * @see com.aol.cyclops.value.Value#toEvalAlways()
		 */
		@Override
		public Eval<T> toEvalAlways() {
			return this;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return get().hashCode();
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof Eval))
				return false;
			return Objects.equals(get(), ((Eval)obj).get());
		}
		@Override
		public String toString(){
			return mkString();
		}
		
	}
}
