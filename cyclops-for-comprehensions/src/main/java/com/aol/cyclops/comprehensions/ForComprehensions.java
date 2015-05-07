package com.aol.cyclops.comprehensions;

import java.util.function.Function;

import lombok.AllArgsConstructor;

import com.aol.cyclops.comprehensions.FreeFormForComprehension.ComphrensionData;

/**
 * Static helper methods for for comprehensions
 * This class aims to make using for comprehenions as succint as possible
 * 
 * @author johnmcclean
 *
 */
public class ForComprehensions {

	
	/**
	 * Create  for comprehension over a single Monad or collection
	 * 
	 * @param fn for comprehension
	 * @return Result
	 */
	public static <R> R foreach1(Function<LessTypingForComprehension1.Step1<?,R>,R> fn){
		return (R)LessTypingForComprehension1.foreach((Function)fn);
	}
	/**
	 * Create  for comprehension over two Monads or collections
	 * 
	 * @param fn  for comprehension
	 * @return Result
	 */
	public static <R> R foreach2(Function<LessTypingForComprehension2.Step1<?,R>,R> fn){
		return (R)LessTypingForComprehension2.foreach((Function)fn);
	}
	/**
	 * Create  for comprehension over three Monads or collections
	 * 
	 * @param fn for comprehension
	 * @return Result
	 */
	public static <R> R foreach3(Function<LessTypingForComprehension3.Step1<?,R>,R> fn){
		return (R)LessTypingForComprehension3.foreach((Function)fn);
	}
	
	/**
	 * Create  for comprehension over four Monads or collections
	 * 
	 * @param fn for comprehension
	 * @return Result
	 */
	public static <R> R foreach4(Function<LessTypingForComprehension4.Step1<?,R>,R> fn){
		return (R)LessTypingForComprehension4.foreach((Function)fn);
	}
	
	/**
	 * Create a custom for comprehension virtually unlimited in nesting depths
	 * 
	 * @param fn For comprehension
	 * @return Result
	 */
	public static <R> R foreachX(Function<ComphrensionData<?,R>,R> fn){
		return (R)FreeFormForComprehension.foreach((Function)fn);
	}
	
	/**
	 * Create a for comprehension using a custom interface 
	 * 
	 * @param c Interface that defines for comprehension - should extend CustomForComprehension
	 * @param fn for comprehension
	 * @return Result
	 */
	public static <X,R> R foreachX(Class<X> c,Function<X,R> fn){
		return FreeFormForComprehension.foreach(c,fn);
	}
	/**
	 * Step builder for Creating a for comprehension using a custom interface
	 * 
	 * @param c Interface that defines for comprehension - should extend CustomForComprehension
	 * @return next stage in the step builder
	 */
	public static <X> MyComprehension<X> custom(Class<X> c){
		
	 return   new MyComprehension<X>(c);
	}
	
	
	
	
}
