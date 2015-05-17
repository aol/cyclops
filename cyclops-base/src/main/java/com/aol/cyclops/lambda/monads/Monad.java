package com.aol.cyclops.lambda.monads;

import static com.aol.cyclops.lambda.api.AsStreamable.asStreamable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.aol.cyclops.comprehensions.comprehenders.Comprehenders;



/**
 * Trait that encapsulates any Monad type
 * 
 * A generalised view into Any Monad (that implements flatMap or bind and accepts any function definition
 * with an arity of 1).
 * 
 * NB the intended use case is to wrap already existant Monad-like objects from diverse sources, to improve
 * interoperability
 * 
 * @author johnmcclean
 *
 * @param <T>
 * @param <MONAD>
 */
public interface Monad<T,MONAD> extends Functor<T>, Filterable<T>{
	
	public <T,MONAD> Monad<T,MONAD> withMonad(Object invoke);
	public Object getMonad();
	
	default <T> Monad<T,MONAD> withFunctor(T functor){
		return withMonad(functor);
	}
	default Object getFunctor(){
		return getMonad();
	}
	default Filterable<T> withFilterable(Filterable filter){
		return withMonad(filter);
	}
	

	default Object getFilterable(){
		return getMonad();
	}
	default   Monad<T,MONAD>  filter(Predicate<T> fn){
		return (Monad)Filterable.super.filter(fn);
	}
	default  <R> Monad<R,MONAD> map(Function<T,R> fn){
		return (Monad)Functor.super.map(fn);
	}
	default   Monad<T,MONAD>  peek(Consumer<T> c) {
		return (Monad)Functor.super.peek(c);
	}
	
	default <R,NT> Monad<NT,R> bind(Function<T,R> fn){
		return withMonad((MONAD)new ComprehenderSelector().selectComprehender(Comprehenders.Companion.instance.getComprehenders(),
				getMonad())
				.executeflatMap(getMonad(), fn));
	
	}
	/**
	 * Unwrap this Monad into a Stream.
	 * If the underlying monad is a Stream it is returned
	 * If it is an iterable a new Stream is created from it
	 * If it is not a stream or an iterable a (cached) attempt will be made to invokeDynamic a stream() to toStream() method
	 * If there is no stream() method, the monad will be decomposed to an iterable and that stream will be returned
	 * 
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	default Stream<T> stream(){
		return asStreamable((T)getMonad()).stream();
		
	}

	default <R extends MONAD,NT> Monad<NT,R> flatMap(Function<T,R> fn) {
		return bind(fn);
	}
	default  <T> T unwrap(){
		return (T)getMonad();
	}
	

}
