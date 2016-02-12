package com.aol.cyclops.control;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.hamcrest.Matcher;

import com.aol.cyclops.Reducer;
import com.aol.cyclops.Semigroup;
import com.aol.cyclops.control.Xor.Primary;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.functions.CurryVariance;
import com.aol.cyclops.functions.QuadFunction;
import com.aol.cyclops.functions.QuintFunction;
import com.aol.cyclops.functions.TriFunction;
import com.aol.cyclops.internal.matcher2.CheckValues;
import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.types.ConvertableFunctor;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.ToAnyM;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.types.applicative.Applicativable;
import com.aol.cyclops.types.applicative.Applicative;
import com.aol.cyclops.types.applicative.Applicative2;
import com.aol.cyclops.types.applicative.Applicative3;
import com.aol.cyclops.types.applicative.Applicative4;
import com.aol.cyclops.types.applicative.Applicative5;
import com.aol.cyclops.types.applicative.ApplicativeBuilder;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;


public interface Maybe<T> extends Value<T>, 
								Supplier<T>, 
								ConvertableFunctor<T>, 
								Filterable<T>,
								Applicativable<T>,
								ToAnyM<T>{

	
	final static Maybe EMPTY = new Nothing<>();
	
	static <T> Maybe<T> none(){
		return  EMPTY;
	}
	
	static <T> Maybe<T> fromOptional(Optional<T> opt){
		if(opt.isPresent())
			return Maybe.of(opt.get());
		return none();
	}
	
	static <T> Maybe<T> fromEvalOf(Eval<T> eval){
		return new Just<T>(eval);
	}
	static <T> Maybe<T> of(T value){
		Objects.requireNonNull(value);
		return new Just<T>(Eval.later(()->value));
	}
	
	static <T> Maybe<T> ofNullable(T value){
	
		if(value!=null)
			return of(value);
		return none();	
	}

	static <T> Maybe<T> narrow(Maybe<? extends T> broad){
		return (Maybe<T>)broad;
	}
	
	public static <T> Maybe<ListX<T>> sequence(CollectionX<Maybe<T>> maybes){
		return AnyM.sequence(AnyM.<T>listFromMaybe(maybes)).unwrap();
	}
	
	public static <T,R> Maybe<R> accumulateJust(CollectionX<Maybe<T>> maybes,Reducer<R> reducer){
		return sequence(maybes).map(s->s.mapReduce(reducer));
	}
	public static <T,R> Maybe<R> accumulateJust(CollectionX<Maybe<T>> maybes,Function<? super T, R> mapper,Semigroup<R> reducer){
		return sequence(maybes).map(s->s.map(mapper).reduce(reducer.reducer()).get());
	}
	public static <T> Maybe<T> accumulateJust(CollectionX<Maybe<T>> maybes,Semigroup<T> reducer){
		return sequence(maybes).map(s->s.reduce(reducer.reducer()).get());
	}
	default <T> Maybe<T> unit(T unit){
		return  Maybe.of(unit);
	}
	
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.value.Value#toMaybe()
	 */
	@Override
	default Maybe<T> toMaybe() {
		return this;
	}

	
	boolean isPresent();
	
	Maybe<T> recover(Supplier<T> value);
	Maybe<T> recover(T value);
	<R> Maybe<R> map(Function<? super T, ? extends R> mapper);
	<R>  Maybe<R> flatMap(Function<? super T, ? extends Maybe<? extends R>> mapper);
	<R> R visit(Function<? super T,? extends R> some, 
						Supplier<? extends R> none);
	
	
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#filter(java.util.function.Predicate)
	 */
	@Override
	Maybe<T> filter(Predicate<? super T> fn);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#ofType(java.lang.Class)
	 */
	@Override
	default <U> Maybe<U> ofType(Class<U> type) {
		
		return (Maybe<U>)Filterable.super.ofType(type);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#filterNot(java.util.function.Predicate)
	 */
	@Override
	default Maybe<T> filterNot(Predicate<? super T> fn) {
		
		return (Maybe<T>)Filterable.super.filterNot(fn);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#notNull()
	 */
	@Override
	default Maybe<T> notNull() {
		
		return (Maybe<T>)Filterable.super.notNull();
	}

	


	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#cast(java.lang.Class)
	 */
	@Override
	default <U> Maybe<U> cast(Class<U> type) {
		
		return (Maybe<U>)Applicativable.super.cast(type);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#peek(java.util.function.Consumer)
	 */
	@Override
	default Maybe<T> peek(Consumer<? super T> c) {
		
		return (Maybe<T>)Applicativable.super.peek(c);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#trampoline(java.util.function.Function)
	 */
	@Override
	default <R> Maybe<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
		
		return (Maybe<R>)Applicativable.super.trampoline(mapper);
	}
	@Override
	default <R> Maybe<R> patternMatch(R defaultValue,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> case1) {
		
		return (Maybe<R>)Applicativable.super.patternMatch(defaultValue, case1);
	}




	@AllArgsConstructor(access=AccessLevel.PRIVATE)
	public static final class Just<T> implements Maybe<T>{
		
		private final Eval<T> lazy;
		
		
		public <R> Maybe<R> map(Function<? super T, ? extends R> mapper){
			return new Just<>(lazy.map(t->mapper.apply(t)));
		}
		public <R>  Maybe<R> flatMap(Function<? super T, ? extends Maybe<? extends R>> mapper){
			return narrow(mapper.apply(lazy.get()));
			
		}
		public Maybe<T> filter(Predicate<? super T> test){
			if(test.test(lazy.get()))
				return this;
			return EMPTY;
		}
		public <R> R visit(Function<? super T,? extends R> some, 
				Supplier<? extends R> none){
			return map(some).get();
		}
		public Maybe<T> recover(T value){
			return this;
		}
		public Maybe<T> recover(Supplier<T> value){
			return this;
		}
		public String toString(){
			return mkString();
		}
		public T  get(){
			return lazy.get();
		}
		public boolean isPresent(){
			return true;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return Objects.hashCode(lazy.get());
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof Just)
				return Objects.equals(lazy.get(),((Just)obj).get());
			return false;
		}
		
	}
	
	public static class Nothing<T> implements Maybe<T>{
		
		public <R> Maybe<R> map(Function<? super T, ? extends R> mapper){
			return EMPTY;
		}
		public <R>  Maybe<R> flatMap(Function<? super T, ? extends Maybe<? extends R>> mapper){
			return EMPTY;
			
		}
		public Maybe<T> filter(Predicate<? super T> test){
			return EMPTY;
		}
		public T  get(){
			return  Optional.<T>ofNullable(null).get();
		}
		public Maybe<T> recover(T value){
			return Maybe.of(value);
		}
		public Maybe<T> recover(Supplier<T> value){
			return new Just<>(Eval.later(value));
		}
		public <R> R visit(Function<? super T,? extends R> some, 
				Supplier<? extends R> none){
			return none.get();
		}
		public Optional<T> toOptional(){
			return Optional.ofNullable(null);
		}
		public String toString(){
			return mkString();
		}
		public boolean isPresent(){
			return false;
		}
	}
	
}
