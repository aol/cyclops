package com.aol.cyclops.matcher2;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
//import com.aol.cyclops.matcher.builders.ADTPredicateBuilder;
/**
 * An interface / trait for building functionally compositional pattern matching cases
 * 
 * Consists of a Predicate and a Function
 * When match is called, if the predicate holds the function is executed
 * 
 * 
 * @author johnmcclean
 *
 * @param <T> Input type for predicate and function (action)
 * @param <R> Return type for function (action) which is executed if the predicate tests positive
 * @param <X> Type of Function - cyclops pattern matching builders use ActionWithReturn which is serialisable and retains type info
 */
public interface Case<T,R>  {
	/**
	 * @return true is this an 'empty' case
	 */
	public boolean isEmpty();
	/**
	 * @return Pattern for this case
	 */
	public Tuple2<Predicate<? super T>,Function<? super T,? extends R>> get();
	
	/**
	 * @return Predicate for this case
	 */
	default Predicate<? super T> getPredicate(){
		return get().v1;
	}
	/**
	 * @return Action (Function) for this case
	 */
	default Function<? super T, ? extends R> getAction(){
		return get().v2;
	}
	
	

	/**
	 * @return true if this not an EmptyCase
	 */
	default boolean isNotEmpty(){
		return !this.isEmpty();
	}
	/**
	 * Match against the supplied value.
	 * Value will be passed into the current predicate
	 * If it passes / holds, value will be passed to the current function.
	 * The result of function application will be returned wrapped in an Optional.
	 * If the predicate does not hold, Optional.empty() is returned.
	 * 
	 * @param value To match against
	 * @return Optional.empty if doesn't match, result of the application of current function if it does wrapped in an Optional
	 */
	default Optional<R> match(T value){
		if(get().v1.test(value))
			return Optional.of(get().v2.apply(value));
		return Optional.empty();
	}
	/**
	 * Similar to Match, but executed asynchonously on supplied Executor.
	 * 
	 * @see #match
     *
	 * Match against the supplied value.
	 * Value will be passed into the current predicate
	 * If it passes / holds, value will be passed to the current function.
	 * The result of function application will be returned wrapped in an Optional.
	 * If the predicate does not hold, Optional.empty() is returned.
     *
	 * @param executor Executor to execute matching on
	 * @param value Value to match against
	 * @return A CompletableFuture that will eventual contain an Optional.empty if doesn't match, result of the application of current function if it does
	 */
	default CompletableFuture<Optional<R>> matchAsync(Executor executor, T value){
		return CompletableFuture.supplyAsync(()->match(value),executor);
	}
	/**
	 * Construct an instance of Case from supplied predicate and action
	 * 
	 * @param predicate That will be used to match
	 * @param action Function that is executed on succesful match
	 * @return New Case instance
	 */
	public static <T,R> Case<T,R> of(Predicate<? super T> predicate,Function<? super T, ? extends R> action){
		return new ActiveCase<T,R>(Tuple.tuple(predicate,action));
	}
	/**
	 *  Construct an instance of Case from supplied Tuple of predicate and action
	 * 
	 * @param pattern containing the predicate that will be used to match  and the function that is executed on succesful match
	 * @return New Case instance
	 */
	public static <T,R> Case<T,R> of(Tuple2<Predicate<? super T>,Function<? super T, ? extends R>> pattern){
		return new ActiveCase<>(pattern);
	}
	
	public static final Case empty = new EmptyCase();
	/**
	 * @return EmptyCase
	 */
	public static <T,R,X extends Function<T,R>> Case<T,R> empty(){
		return empty;
	}
}
