package com.aol.cyclops.control;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.aol.cyclops.functions.QuadFunction;
import com.aol.cyclops.functions.QuintFunction;
import com.aol.cyclops.functions.TriFunction;
import com.aol.cyclops.functions.currying.CurryVariance;
import com.aol.cyclops.lambda.applicative.Applicativable;
import com.aol.cyclops.lambda.applicative.Applicative;
import com.aol.cyclops.lambda.applicative.Applicative2;
import com.aol.cyclops.lambda.applicative.Applicative3;
import com.aol.cyclops.lambda.applicative.Applicative4;
import com.aol.cyclops.lambda.applicative.Applicative5;
import com.aol.cyclops.lambda.applicative.ApplicativeBuilder;
import com.aol.cyclops.lambda.monads.ConvertableFunctor;
import com.aol.cyclops.lambda.monads.Filterable;
import com.aol.cyclops.value.Value;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;


public interface Maybe<T> extends Value<T>, Supplier<T>, ConvertableFunctor<T>, Filterable<T>,Applicativable<T>{

	static <T,R> ApplicativeBuilder<T,R,Maybe<R>> applicativeBuilder(){
		return new ApplicativeBuilder<T,R,Maybe<R>> (Maybe.of(1));
	}
	final static Maybe EMPTY = new Nothing();
	
	static <T> Maybe<T> none(){
		return EMPTY;
	}
	
	static <T> Maybe<T> fromOptional(Optional<T> opt){
		if(opt.isPresent())
			return Maybe.of(opt.get());
		return none();
	}
	
	static <T> Maybe<T> fromEvalSome(Eval<T> eval){
		return new Just<T>(eval);
	}
	static <T> Maybe<T> of(T value){
		Objects.requireNonNull(value);
		return new Just<T>(Eval.later(()->value));
	}
	static Integer add(Integer i){
		return i+1;
	}
	static <T> Maybe<T> ofNullable(T value){
	
		if(value!=null)
			return of(value);
		return none();	
	}

	static <T> Maybe<T> narrow(Maybe<? extends T> broad){
		return (Maybe<T>)broad;
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

	/**
	 * <pre>
	 * {@code 
	 *  Maybe.of(1).ap1(applicative( (Integer i)->i+1));
	 * 
	 * }
	 * </pre>
	 * 
	 * @param ap
	 * @return
	 */
	default <R> Maybe<R> ap1( Applicative<T,R, ?> ap){
		return (Maybe<R>)Applicativable.super.ap1(ap);
	}

	boolean isPresent();
	
	Maybe<T> recover(Supplier<T> value);
	Maybe<T> recover(T value);
	<R> Maybe<R> map(Function<? super T, ? extends R> mapper);
	<R>  Maybe<R> flatMap(Function<? super T, ? extends Maybe<? extends R>> mapper);
	<R> R when(Function<? super T,? extends R> some, 
						Supplier<? extends R> none);
	
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
		public <R> R when(Function<? super T,? extends R> some, 
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
		public <R> R when(Function<? super T,? extends R> some, 
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
