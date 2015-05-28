package com.aol.cyclops.lambda.monads;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aol.cyclops.comprehensions.comprehenders.Comprehenders;
import com.aol.cyclops.lambda.api.AsStreamable;
import com.aol.cyclops.lambda.api.Streamable;
import com.aol.cyclops.streams.StreamUtils;



/**
 * An interoperability Trait that encapsulates java Monad implementations.
 * 
 * A generalised view into Any Monad (that implements flatMap or bind and accepts any function definition
 * with an arity of 1).
 * 
 * NB the intended use case is to wrap already existant Monad-like objects from diverse sources, to improve
 * interoperability - it's not intended for use as an interface to be implemented on a Monad.
 * 
 * @author johnmcclean
 *
 * @param <T>
 * @param <MONAD>
 */
public interface Monad<T,MONAD> extends Functor<T>, Filterable<T>, Streamable<T>{
	
	
	
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
	
	default <R> Monad<T,MONAD> bind(Function<T,R> fn){
		return withMonad((MONAD)new ComprehenderSelector().selectComprehender(Comprehenders.Companion.instance.getComprehenders(),
				getMonad())
				.executeflatMap(getMonad(), fn));
	
	}
	/**
	 * join / flatten one level of a nested hierarchy
	 * 
	 * @return Flattened / joined one level
	 */
	default <T1> Monad<T1,T> flatten(){
		return (Monad)this.flatMap( t->   (MONAD)t );
		
	}
	
	/**
	 * @return Underlying monad converted to a Streamable instance
	 */
	default Streamable<T> toStreamable(){
		return  AsStreamable.asStreamable(stream());
	}
	/**
	 * @return This monad converted to a set
	 */
	default Set<T> toSet(){
		return (Set)stream().collect(Collectors.toSet());
	}
	/**
	 * @return this monad converted to a list
	 */
	default List<T> toList(){
		return (List)stream().collect(Collectors.toList());
	}
	/**
	 * Unwrap this Monad into a Stream.
	 * If the underlying monad is a Stream it is returned
	 * Otherwise we flatMap the underlying monad to a Stream type
	 */
	default Stream<T> stream(){
		Stream stream = Stream.of(1);
		return this.<T,Stream>withMonad((Stream)new ComprehenderSelector().selectComprehender(Comprehenders.Companion.instance.getComprehenders(),
				stream).executeflatMap(stream, i-> getMonad())).unwrap();
		
	}
	/**
	 * @return This monad coverted to an Optional
	 */
	default Optional<T> toOptional(){
		Optional stream = Optional.of(1);
		return this.<T,Optional>withMonad((Optional)new ComprehenderSelector().selectComprehender(Comprehenders.Companion.instance.getComprehenders(),
				stream).executeflatMap(stream, i-> getMonad())).unwrap();
		
	}
	
	/**
	 * Convert to a Stream with the values repeated specified times
	 * 
	 * @param times Times values should be repeated within a Stream
	 * @return Stream with values repeated
	 */
	default Stream<T> cycle(int times){
		
		return StreamUtils.cycle(times,AsStreamable.asStreamable(stream()));
		
	}
	

	default <R extends MONAD,NT> Monad<NT,R> flatMap(Function<T,R> fn) {
		return (Monad)bind(fn);
	}
	default  MONAD unwrap(){
		return (MONAD)getMonad();
	}
	

}
